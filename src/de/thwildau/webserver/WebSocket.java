package de.thwildau.webserver;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import xuggler.DesktopStream;
import de.thwildau.model.UserData;
import de.thwildau.server.AmberServer;
import de.thwildau.stream.StreamManager;
import de.thwildau.stream.VideoStreamer;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;

@ServerEndpoint("/websocket/")
public class WebSocket {

	private ConcurrentHashMap<Session, DesktopStream> streams = new ConcurrentHashMap<Session, DesktopStream>();
	private ConcurrentHashMap<Session, Integer> sessions = new ConcurrentHashMap<Session, Integer>();


	@OnMessage
	public void onMessage(String message, Session session) 
			throws IOException, InterruptedException {

		Object[] request = parseRequest(message);

		String responseMessage = null;
		String ident = (String) request[0];
		switch(ident){
		case "startStream":
			String vehicleID = (String) request[1];
			StreamManager.getStream(vehicleID).startStream(session);
			//			DesktopStream stream = new DesktopStream(session, vehicleID);
			//			stream.startStream();
			//			streams.put(session, stream);
			responseMessage = new WebsocketResponse(WebsocketResponse.STREAM_STARTED, Constants.SUCCESS_STREAM_STARTED).toJSON();
			//			session.getBasicRemote().sendText(responseMessage);			
			break;
		case "stopStream":
			vehicleID = (String) request[1];
			StreamManager.getStream(vehicleID).stopStream();
			//streams.get(session).stopStream();
			responseMessage = new WebsocketResponse(WebsocketResponse.STREAM_CLOSED, Constants.SUCCESS_STREAM_CLOSED).toJSON();
			session.getBasicRemote().sendText(responseMessage);
			break;
		case "startRecord":
			//			vehicleID = (String) request[1];
			Object[] recordContent = parseRecordRequest((JSONObject) request[1]);
			vehicleID = (String)recordContent[0];
			int userID = (int) recordContent[1];
			VideoStreamer streamer = StreamManager.getStream(vehicleID);
			if(streamer != null)
				streamer.startRecord(userID, vehicleID);
			break;
		case "stopRecord":
			vehicleID = (String) request[1];
			streamer = StreamManager.getStream(vehicleID);
			if(streamer != null)
				streamer.stopRecord(vehicleID);
			break;
		case "requestLogin":
			Object[] loginContent = parseLoginRequest((JSONObject) request[1]);
			String usernameLogin = (String)loginContent[0];
			byte[] passLogin = passwordToHash((String)loginContent[1]);
			// Check for User and Password
			int user_id = AmberServer.getDatabase().adminLogin(usernameLogin, passLogin);
			// No match with username and password
			if(user_id == -1){				
				responseMessage = new WebsocketResponse(WebsocketResponse.ERROR, Constants.ERROR_LOGIN).toJSON();
				ServerLogger.log("Web App Login failed - Wrong Username or Password: " + usernameLogin, Constants.DEBUG);
			}
			// User is not allowed to visit the web app
			else if (user_id == -2){
				responseMessage = new WebsocketResponse(WebsocketResponse.ERROR, Constants.ERROR_ADMIN).toJSON();
				ServerLogger.log("Web App Login failed - No Admin: " + usernameLogin, Constants.DEBUG);
			}
			// Access granted
			else{				
				UserData response = new UserData();
				response = response.prepareUserData(user_id);
				responseMessage = new WebsocketResponse(WebsocketResponse.LOGIN, response).toJSON();
				sessions.put(session, user_id);
				ServerLogger.log("Web App Login success: " + usernameLogin, Constants.DEBUG);
			}
			session.getBasicRemote().sendText(responseMessage);
			break;
		case "requestLogout":
			user_id = safeLongToInt((long)request[1]);
			AmberServer.getDatabase().logout(user_id);
			logoutUser(session);
			responseMessage = new WebsocketResponse(WebsocketResponse.LOGOUT, "Logout succeeded").toJSON();
			session.getBasicRemote().sendText(responseMessage);
			break;
		case "requestCars":
			String[] carsContent = parseCarsRequest((JSONObject) request[1]);
			StreamManager.getStream(carsContent[0]);
			break;
		default:
			break;
		}
	}
	@OnOpen
	public void onOpen(Session session) {
		ServerLogger.log("Web App Client connected: " + session.getId(), Constants.DEBUG);
	}

	@OnClose
	public void onClose(Session session) {
		for(Session s : streams.keySet())
			if(s.equals(session)){
				streams.get(session).stopStream();
				streams.remove(session);
			}
		ServerLogger.log("Wep App Connection closed: " + session.getId(), Constants.DEBUG);
	}

	private Object[] parseRequest(String message){
		System.out.println(message);
		Object[] request = null;
		JSONParser parser = new JSONParser();
		Object resultObject = null;
		try {
			resultObject = parser.parse(message);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (resultObject instanceof JSONObject) {
			JSONObject obj = (JSONObject)resultObject;
			request = new Object[obj.keySet().size()];
			request[0] = (String) obj.get("callID");
			if((request.length > 1) )
				request[1] = obj.get("data");
		}
		return request;
	}
	/**
	 * 
	 * @param jsonObject
	 * @return
	 */
	private String[] parseLoginRequest(JSONObject jsonObject) {
		String[] request = null;

		if (jsonObject instanceof JSONObject) {
			request = new String[jsonObject.keySet().size()];
			request[0] = (String) jsonObject.get("email");
			request[1] = (String) jsonObject.get("password");
		}
		return request;
	}
	/**
	 * 
	 * @param jsonObject
	 * @return
	 */
	private String[] parseCarsRequest(JSONObject jsonObject) {
		String[] request = null;

		if (jsonObject instanceof JSONObject) {
			request = new String[jsonObject.keySet().size()];
			request[0] = (String) jsonObject.get("vehicleID");
		}
		return request;
	}	
	/**
	 * 
	 * @param jsonObject
	 * @return
	 */
	private Object[] parseRecordRequest(JSONObject jsonObject) {
		Object[] request = null;

		if (jsonObject instanceof JSONObject) {
			request = new Object[2];
			request[0] = (String) jsonObject.get("vehicleID");
			request[1] = safeLongToInt((long)jsonObject.get("userID"));
		}
		return request;
	}	
	private void logoutUser(Session session) {
		if(streams.get(session) != null)
			streams.get(session).stopStream();

	}

	private byte[] passwordToHash(String pass){
		byte[] hashed = null;
		try {
			// Create MessageDigest instance for MD5
			MessageDigest md = MessageDigest.getInstance("MD5");
			//Add password bytes to digest
			md.update(pass.getBytes());
			//Get the hash's bytes 
			hashed = md.digest();            
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		return hashed;
	}
	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}
}