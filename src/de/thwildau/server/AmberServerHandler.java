package de.thwildau.server;


import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import de.thwildau.info.ClientMessage;
import de.thwildau.info.ClientMessage.Ident;
import de.thwildau.model.Event;
import de.thwildau.model.User;
import de.thwildau.model.UserData;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;

public class AmberServerHandler extends IoHandlerAdapter
{

	private static final boolean DEBUG = true;
	private static AmberServerHandler handler = null;
	private ConcurrentHashMap<IoSession, String> sessions = new ConcurrentHashMap<IoSession, String>();

	public static AmberServerHandler getInstance(){
		if(handler==null)
			handler = new AmberServerHandler();
		return handler;
	}

	public void sessionCreated(IoSession session) {
		ServerLogger.log("Session created..." + session, DEBUG);
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
	}

	public void sessionClosed(IoSession session) throws Exception {
		ServerLogger.log("Session closed...", DEBUG);
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
		// Check for incoming Message from OBU or Application
		ClientMessage receivedMessage = null;
		if(message instanceof ClientMessage)		// Application
			receivedMessage = (ClientMessage) message;
		else if (message instanceof String){		// OBU
			System.out.println("Message: " + message);
			return;
		}

		ServerLogger.log("received message from... " + session.getId(), DEBUG);

		//
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
			// Database validation
			// Check for User and Password
			int user_id = AmberServer.getDatabase().login(usernameLogin, passLogin);
			if(user_id == -1){
				responseMessage = new ClientMessage(ClientMessage.Ident.ERROR, Constants.ERROR_LOGIN);
				ServerLogger.log("Login failed: " + usernameLogin, DEBUG);
			}
			// Check for GCM Registration
			else{
				boolean queryRegisterGCM = AmberServer.getDatabase().registerGCM(user_id, regID);
				// GCM Registration failed
				if(!queryRegisterGCM){
					responseMessage = new ClientMessage(ClientMessage.Ident.ERROR, Constants.ERROR_GCM);					
					ServerLogger.log("GCM failed: " + usernameLogin, DEBUG);
				}	
				// Everything ok - Login succeeded
				else{
					UserData response = new UserData();
					response = response.prepareUserData(user_id);
					responseMessage = new ClientMessage(ClientMessage.Ident.LOGIN, response);				
					ServerLogger.log("Login success: " + usernameLogin, DEBUG);
				}
			}
			session.write(responseMessage);
			break;
		case REGISTER:
			String usernameRegister = ((User)receivedMessage.getContent()).getName();
			byte[] passRegister = ((User)receivedMessage.getContent()).getPass();
			boolean queryRegister = AmberServer.getDatabase().addUser(usernameRegister, passRegister);
			// Registration failed
			if(!queryRegister){
				responseMessage = new ClientMessage(ClientMessage.Ident.ERROR, Constants.ERROR_REGISTER);
				ServerLogger.log("Registration failed: " + usernameRegister, DEBUG);
			}
			// Registration succeeded
			else{
				responseMessage = new ClientMessage(ClientMessage.Ident.REGISTER, Constants.SUCCESS_ARG_REGISTER);
				ServerLogger.log("Registration success: " + usernameRegister, DEBUG);				
			}
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