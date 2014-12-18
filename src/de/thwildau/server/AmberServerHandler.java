package de.thwildau.server;


import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import de.thwildau.gcm.SendNotification;
import de.thwildau.info.ClientMessage;
import de.thwildau.info.OBUMessage;
import de.thwildau.model.Event;
import de.thwildau.model.User;
import de.thwildau.model.UserData;
import de.thwildau.model.Vehicle;
import de.thwildau.obu.OBUResponseHandler;
import de.thwildau.stream.VideoStreamer;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;

public class AmberServerHandler extends IoHandlerAdapter
{

	private static final boolean DEBUG = true;
	private static final int TIMEOUT = 5 * 60 * 1000;

	private static AmberServerHandler handler = null;
	private static OBUResponseHandler obuHandler = new OBUResponseHandler("Stream");
	private ConcurrentHashMap<IoSession, String> sessions = new ConcurrentHashMap<IoSession, String>();
	private ConcurrentHashMap<Integer, TimerTask> timers = new ConcurrentHashMap<Integer, TimerTask>();
	private TimerTask task;
	private VideoStreamer streamer;
	private Thread videoThread;

	public static AmberServerHandler getInstance(){
		if(handler==null)
			handler = new AmberServerHandler();
		return handler;
	}

	public void sessionCreated(IoSession session) {
		ServerLogger.log("Session created..." + session, DEBUG);
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 5);
		
//		streamer = new VideoStreamer();
//		videoThread = new Thread(streamer);
//		videoThread.start();
//		StreamManager.addStream("0", streamer);

		byte[] b = {(byte)0x01};
		session.write(new OBUMessage(OBUMessage.REQUEST_TELEMETRY, b).request);

	}

	public void sessionClosed(IoSession session) throws Exception {
		ServerLogger.log("Session closed...", DEBUG);
		if(session.getAttribute("user") != null){
			setTimeout((int) session.getAttribute("user"));
		}
	}

	@Override
	public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
	{
		cause.printStackTrace();
	}

	public void messageWrite(Object message){

	}
	/**
	 * Handle received messages from a Android Application.
	 * 
	 * @param session	Session of the User.
	 * @param message	Incoming Message from User.
	 * @throws Exception
	 */
	@Override
	public void messageReceived( IoSession session, Object message ) throws Exception
	{

		
		// Check for incoming Message from OBU or Application
		ClientMessage receivedClientMessage = null;

		if(message instanceof ClientMessage)		// Application
			receivedClientMessage = (ClientMessage) message;
		else{ 										// OBU
			streamer.setImage((byte[]) message);
//			boolean transactionState = obuHandler.addImageToStream((byte[]) message);
			
//			if(transactionState){
//				byte[] b = {(byte)0x01};
//				session.write(new OBUMessage(OBUMessage.REQUEST_TELEMETRY, b).request);
//			}
//			else
//				ServerLogger.log("ERROR WHILE RECEIVING STREAM DATA", DEBUG);

			return;
		}

		switch(receivedClientMessage.getId()){
		case LOGIN_CHECK:
			try{
				//				lock.lock();
				int user_id = (int)receivedClientMessage.getContent();
				UserData response = null;
				if(user_id == -1){
					session.write(new ClientMessage(ClientMessage.Ident.LOGIN_CHECK, response));
					break;
				}
				boolean isOnline = AmberServer.getDatabase().checkOnline(user_id);
				if(isOnline){
					session.setAttribute("user", user_id);
					cancelTimeout(user_id);
					response = new UserData().prepareUserData(user_id);
					session.write(new ClientMessage(ClientMessage.Ident.LOGIN_CHECK, response));
				}
				else
					session.write(new ClientMessage(ClientMessage.Ident.LOGIN_CHECK, response));
			}
			finally{
				//				lock.unlock();
			}
			break;
		case LOGIN:
			ClientMessage responseMessage = null;
			String usernameLogin = ((User)receivedClientMessage.getContent()).getName();
			byte[] passLogin = ((User)receivedClientMessage.getContent()).getPass();
			String regID = ((User)receivedClientMessage.getContent()).getRegistationID();
			// Database validation
			// Check for User and Password
			int userID = AmberServer.getDatabase().login(usernameLogin, passLogin);
			if(userID == -1){
				responseMessage = new ClientMessage(ClientMessage.Ident.ERROR, Constants.ERROR_LOGIN);
				ServerLogger.log("Login failed: " + usernameLogin, DEBUG);
			}
			// Check for GCM Registration
			else{
				//				// GCM Registration failed
				//				if(!queryRegisterGCM){
				//					responseMessage = new ClientMessage(ClientMessage.Ident.ERROR, Constants.ERROR_GCM);					
				//					ServerLogger.log("GCM failed: " + usernameLogin, DEBUG);
				//				}	
				//				// Everything ok - Login succeeded
				//				else{
				AmberServer.getDatabase().registerGCM(userID, regID);
				UserData response = new UserData();
				response = response.prepareUserData(userID);
				responseMessage = new ClientMessage(ClientMessage.Ident.LOGIN, response);
				session.setAttribute("user", userID);
				ServerLogger.log("Login success: " + usernameLogin, DEBUG);
				//			}
			}
			session.write(responseMessage);
			break;
		case REGISTER:
			String usernameRegister = ((User)receivedClientMessage.getContent()).getName();
			byte[] passRegister = ((User)receivedClientMessage.getContent()).getPass();
			boolean queryRegister = AmberServer.getDatabase().addUser(usernameRegister, passRegister);
			// Registration failed
			if(!queryRegister){
				responseMessage = new ClientMessage(ClientMessage.Ident.ERROR, Constants.ERROR_REGISTER);
				ServerLogger.log("Registration failed: " + usernameRegister, DEBUG);
			}
			// Registration succeeded
			else{
				responseMessage = new ClientMessage(ClientMessage.Ident.REGISTER, Constants.SUCCESS_REGISTER);
				ServerLogger.log("Registration success: " + usernameRegister, DEBUG);				
			}
			session.write(responseMessage);
			break;
		case EVENT_REQUEST:
			String eventID = (String) receivedClientMessage.getContent();
			Event ev = new Event();
			ev.setVehicleID(eventID);
			session.write(new ClientMessage(ClientMessage.Ident.EVENT, ev));
			break;
		case NOTIFICATION:
			new SendNotification("GCM_Notification from OBU");
			break;
		case REGISTER_VEHICLE:
			Object[] request = (Object[]) receivedClientMessage.getContent();
			userID = (int) request[0];
			String vehicleID = (String) request[1];

			boolean queryAddVehicle = AmberServer.getDatabase().registerVehicle(userID, vehicleID);
			if(!queryAddVehicle){
				responseMessage = new ClientMessage(ClientMessage.Ident.ERROR, Constants.ERROR_REGISTER_VEHICLE);
				ServerLogger.log("Add Vehicle failed: " + userID, DEBUG);
			}
			// Add Vehicle succeeded
			else{
				Vehicle response = new Vehicle();
				response = response.prepareVehicle(vehicleID);
				responseMessage = new ClientMessage(ClientMessage.Ident.REGISTER_VEHICLE, response);
				ServerLogger.log("Add Vehicle succeeded: " + userID, DEBUG);				
			}
			session.write(responseMessage);
			break;
		case UNREGISTER_VEHICLE:
			request = (Object[]) receivedClientMessage.getContent();
			userID = (int) request[0];
			vehicleID = (String) request[1];
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
			obuHandler.writeToFile();
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

	/**
	 * Set a Timeout to log a User out if the application is closed.
	 * 
	 * @param user_id Current User
	 */
	public void setTimeout(int user_id){
		ServerLogger.log("Set Timeout Countdown to 5 min for User " + user_id, DEBUG);
		Timer timer = new Timer();
		task = new TimerTask() {			
			@Override
			public void run() {
				ServerLogger.log("Logout for User " + user_id, DEBUG);
				AmberServer.getDatabase().logout(user_id);
			}
		};
		// Logout in 5 minutes
		timer.schedule(task, TIMEOUT);
		timers.put(user_id, task);
	}
	/**
	 * Cancel running Timer for Logout, when User logs in again.
	 * @param user_id Current User
	 */
	public void cancelTimeout(int user_id){
		TimerTask task = timers.get(user_id);
		if(task != null)
			task.cancel();
	}
}