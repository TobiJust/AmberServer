package de.thwildau.server;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import de.thwildau.info.ClientMessage;
import de.thwildau.info.OBUMessage;
import de.thwildau.model.Event;
import de.thwildau.model.User;
import de.thwildau.model.UserData;
import de.thwildau.model.Vehicle;
import de.thwildau.obu.OBUResponseHandler;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;

/**
 * 
 * @author Tobias Just
 *
 */
public class AmberServerHandler extends IoHandlerAdapter
{

	private static final int TIMEOUT = 5 * 60 * 1000;

	boolean transactionState = false;
	private static AmberServerHandler handler = null;
	private ConcurrentHashMap<IoSession, String> sessions = new ConcurrentHashMap<IoSession, String>();
	private ConcurrentHashMap<Integer, TimerTask> timers = new ConcurrentHashMap<Integer, TimerTask>();
	private ConcurrentHashMap<IoSession, OBUResponseHandler> obuHandlers = new ConcurrentHashMap<IoSession, OBUResponseHandler>();
	private TimerTask task;

	public static AmberServerHandler getInstance(){
		if(handler==null)
			handler = new AmberServerHandler();
		return handler;
	}

	public void sessionCreated(IoSession session) {
		ServerLogger.log("Session created..." + session, Constants.DEBUG);
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 5);


//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		byte[] b = {(byte)0x01};
//		session.write(new OBUMessage(OBUMessage.REQUEST_TELEMETRY, b).request);
//		obuHandlers.put(session, new OBUResponseHandler());
	}

	public void sessionClosed(IoSession session) throws Exception {
		ServerLogger.log("Session closed...", Constants.DEBUG);
	}

	@Override
	public void exceptionCaught( IoSession session, Throwable cause ) throws Exception{
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
			interpretData((byte[]) message, session);
			//			boolean transactionState = obuHandlers.get(session).addData((byte[])message); 
			//			if(transactionState){
			//				byte[] b = {(byte)0x01};
			//				session.write(new OBUMessage(OBUMessage.REQUEST_TELEMETRY, b).request);
			//			}
			//
			//			Thread.sleep(200);

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
					ServerLogger.log("Login succeeded: " + user_id, Constants.DEBUG);
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
				ServerLogger.log("Login failed: " + usernameLogin, Constants.DEBUG);
			}
			// Check for GCM Registration
			else{
				AmberServer.getDatabase().registerGCM(userID, regID);
				UserData response = new UserData();
				response = response.prepareUserData(userID);
				responseMessage = new ClientMessage(ClientMessage.Ident.LOGIN, response);
				session.setAttribute("user", userID);
				ServerLogger.log("Login success: " + usernameLogin, Constants.DEBUG);
			}
			session.write(responseMessage);
			break;
		case LOGOUT:
			int user_id = (int)receivedClientMessage.getContent();
			AmberServer.getDatabase().logout(user_id);
			responseMessage = new ClientMessage(ClientMessage.Ident.LOGOUT, Constants.SUCCESS_LOGOUT);
			session.write(responseMessage);
			break;
		case REGISTER:
			String usernameRegister = ((User)receivedClientMessage.getContent()).getName();
			byte[] passRegister = ((User)receivedClientMessage.getContent()).getPass();
			boolean queryRegister = AmberServer.getDatabase().addUser(usernameRegister, passRegister, 0);
			// Registration failed
			if(!queryRegister){
				responseMessage = new ClientMessage(ClientMessage.Ident.ERROR, Constants.ERROR_REGISTER);
				ServerLogger.log("Registration failed: " + usernameRegister, Constants.DEBUG);
			}
			// Registration succeeded
			else{
				responseMessage = new ClientMessage(ClientMessage.Ident.REGISTER, Constants.SUCCESS_REGISTER);
				ServerLogger.log("Registration success: " + usernameRegister, Constants.DEBUG);				
			}
			session.write(responseMessage);
			break;
		case EVENT_DETAIL:
		case EVENT_REQUEST:
			Object[] request = (Object[]) receivedClientMessage.getContent();
			int eventID = (int) request[0];
			int obuID = (int) request[1];
			Object[] eventData = AmberServer.getDatabase().getEventData(eventID);
			String vehicleName = AmberServer.getDatabase().getVehicle(obuID);
			String eventType = (String)eventData[1];
			String eventTime = (String)eventData[2];
			double eventLat = (double)eventData[3];
			double eventLon = (double)eventData[4];
			byte[] eventImage = (byte[]) eventData[5];	// EventImage
			Event event = new Event(eventType, eventTime, eventLat, eventLon, eventImage, vehicleName);
			event.setVehicleID(obuID);
			session.write(new ClientMessage(receivedClientMessage.getId(), event));
			break;
		case REGISTER_VEHICLE:
			request = (Object[]) receivedClientMessage.getContent();
			userID = (int) request[0];
			int vehicleID = (int) request[1];

			Vehicle vehicle = AmberServer.getDatabase().registerVehicle(userID, vehicleID);
			if(vehicle == null){
				responseMessage = new ClientMessage(ClientMessage.Ident.ERROR, Constants.ERROR_REGISTER_VEHICLE);
				ServerLogger.log("Add Vehicle failed: " + userID, Constants.DEBUG);
			}
			// Add Vehicle succeeded
			else{
				vehicle = vehicle.prepareVehicle(vehicleID);
				responseMessage = new ClientMessage(ClientMessage.Ident.REGISTER_VEHICLE, vehicle);
				ServerLogger.log("Add Vehicle succeeded: " + userID, Constants.DEBUG);				
			}
			session.write(responseMessage);
			break;
		case UNREGISTER_VEHICLE:
			request = (Object[]) receivedClientMessage.getContent();
			userID = (int) request[0];
			vehicleID = (int) request[1];
			int position = (int) request[2];
			boolean queryUnregisterVehicle = AmberServer.getDatabase().unregisterVehicle(userID, vehicleID);
			if(!queryUnregisterVehicle){
				responseMessage = new ClientMessage(ClientMessage.Ident.UNREGISTER_VEHICLE, -1);
				ServerLogger.log("Unregister Vehicle failed for User: " + userID, Constants.DEBUG);
			}
			// Unregister Vehicle succeeded
			else{
				responseMessage = new ClientMessage(ClientMessage.Ident.UNREGISTER_VEHICLE, position);
				ServerLogger.log("Unregister Vehicle succeeded for User: " + userID, Constants.DEBUG);				
			}
			session.write(responseMessage);
			break;
		case TOGGLE_ALARM:
			request = (Object[]) receivedClientMessage.getContent();
			userID = (int) request[0];
			vehicleID = (int) request[1];
			boolean status = (boolean) request[2];
			position = (int) request[3];
			boolean queryToggleAlarm = AmberServer.getDatabase().toggleAlarm(userID, vehicleID, status);
			Object[] responseData = {queryToggleAlarm, position};
			responseMessage = new ClientMessage(ClientMessage.Ident.TOGGLE_ALARM, responseData);
			ServerLogger.log("Toggle Alarm for User: " + userID + " " + queryToggleAlarm, Constants.DEBUG);	
			session.write(responseMessage);
			break;
		case GET_EVENTLIST_BACKPRESS:
		case GET_EVENTLIST:
			vehicleID = (int) receivedClientMessage.getContent();
			ArrayList<Event> events = Vehicle.prepareEventList(vehicleID);
			responseMessage = new ClientMessage(receivedClientMessage.getId(), events);
			session.write(responseMessage);
			break;
		case GET_VEHICLELIST_BACKPRESS:
			userID = (int) receivedClientMessage.getContent();
			UserData response = new UserData();
			response = response.prepareUserData(userID);
			responseMessage = new ClientMessage(ClientMessage.Ident.GET_VEHICLELIST_BACKPRESS, response.getVehicles());
			session.write(responseMessage);
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
			//			obuHandlers.get(session).writeToFile();
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

	private void interpretData(byte[] data, IoSession session) {
		//		FrameObject frame = new FrameObject();
		//		frame.append(data, 0);
		try {
			//			if(frame.getMessageID() == FrameObject.DEVICE){
			//				OBUResponseHandler.handlers.put(frame.getDeviceID(), session);
			//				obuHandlers.put(session, new OBUResponseHandler());
			//			}
			//			else{
			if(obuHandlers.get(session) == null)
				obuHandlers.put(session, new OBUResponseHandler());
			boolean transactionState = obuHandlers.get(session).addData(data);
			if(transactionState){
				byte[] b = {(byte)0x01};
				session.write(new OBUMessage(OBUMessage.REQUEST_TELEMETRY, b).request);
			}
			//			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set a Timeout to log a User out if the application is closed.
	 * 
	 * @param user_id Current User
	 */
	public void setTimeout(int user_id){
		ServerLogger.log("Set Timeout Countdown to 5 min for User " + user_id, Constants.DEBUG);
		Timer timer = new Timer();
		task = new TimerTask() {			
			@Override
			public void run() {
				ServerLogger.log("Logout for User " + user_id, Constants.DEBUG);
				AmberServer.getDatabase().logout(user_id);
			}
		};
		// Logout in in given time
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