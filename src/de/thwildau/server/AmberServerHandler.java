package de.thwildau.server;


import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import de.thwildau.info.ClientMessage;
import de.thwildau.info.ClientMessage.Ident;
import de.thwildau.model.Event;
import de.thwildau.model.User;
import de.thwildau.util.ServerLogger;

public class AmberServerHandler extends IoHandlerAdapter
{

	private static AmberServerHandler handler = null;
	private ConcurrentHashMap<IoSession, String> sessions = new ConcurrentHashMap<IoSession, String>();

	public static AmberServerHandler getInstance(){
		if(handler==null)
			handler = new AmberServerHandler();
		return handler;
	}

	public void sessionCreated(IoSession session) {
		ServerLogger.log("Session created..." + session, true);
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
	}

	public void sessionClosed(IoSession session) throws Exception {
		ServerLogger.log("Session closed...", true);
	}

	@Override
	public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
	{
		cause.printStackTrace();
	}

	public void messageWrite(Object message){

	}
	@Override
	public void messageReceived( IoSession session, Object message ) throws Exception
	{
		ClientMessage receivedMessage = null;
		if(message instanceof ClientMessage)
			receivedMessage = (ClientMessage) message;
		else if (message instanceof String){
			System.out.println("Message: " + message);
			return;
		}

		ServerLogger.log("received message from... " + session.getId(), true);
		System.out.println(receivedMessage.getId());

		switch(receivedMessage.getId()){
		case TEXT_MESSAGE:
			try{
				//				lock.lock();
				System.out.println(new ClientMessage(Ident.TEXT_MESSAGE, sessions.get(session) +": "+(String)receivedMessage.getContent()));
			}
			finally{
				//				lock.unlock();
			}
			break;
		case LOGIN:
			ClientMessage responseMessage = null;
			String usernameLogin = ((User)receivedMessage.getContent()).getName();
			byte[] passLogin = ((User)receivedMessage.getContent()).getPass();
			String regID = ((User)receivedMessage.getContent()).getRegistationID();
			boolean queryRegisterGCM;
			// Database validation
			int user_id = AmberServer.getDatabase().login(usernameLogin, passLogin);
			// TODO: Querys auswerten
			// TODO: String constants
			if(user_id == -1){
				responseMessage = new ClientMessage(ClientMessage.Ident.ERROR, "No such User or Password");
				ServerLogger.log("Login failed -->" + usernameLogin, true);
			}
			else{
				queryRegisterGCM = AmberServer.getDatabase().registerGCM(user_id, regID);
				responseMessage = new ClientMessage(ClientMessage.Ident.LOGIN, "Login succeeded");				
				ServerLogger.log("Login from " + usernameLogin, true);
			}
			session.write(responseMessage);
			break;
		case REGISTER:
			String usernameRegister = ((User)receivedMessage.getContent()).getName();
			byte[] passRegister = ((User)receivedMessage.getContent()).getPass();
			boolean queryRegister = AmberServer.getDatabase().addUser(usernameRegister, passRegister);
			session.write(new ClientMessage(ClientMessage.Ident.REGISTER, queryRegister));
			if(!queryRegister)
				responseMessage = new ClientMessage(ClientMessage.Ident.ERROR, "Registration failed");
			else{
				responseMessage = new ClientMessage(ClientMessage.Ident.REGISTER, "Registration succeeded");				
			}
			ServerLogger.log("Register from " + session.getRemoteAddress().toString(), true);
			session.write(responseMessage);
			break;
		case EVENT_REQUEST:
			String eventID = (String) receivedMessage.getContent();
			Event ev = new Event();
			ev.setVehicleID(eventID);
			session.write(new ClientMessage(ClientMessage.Ident.EVENT, ev));
			
			break;
		default:
			break;
		}
	}

	@Override
	public void sessionIdle( IoSession session, IdleStatus status ) throws Exception
	{
		if(!session.isConnected()){
			closeSession(session);   
		}
	}

	private synchronized void closeSession(IoSession session){
		try{
			//			lock.lock();
			sessions.remove(session);
			session.close(true);
		}
		finally{
			//			lock.unlock();
		}

	}
	public static void clearInstance(){
		handler = null;
	}
}