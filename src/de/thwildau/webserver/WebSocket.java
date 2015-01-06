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

import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import xuggler.DesktopStream;
import de.thwildau.model.UserData;
import de.thwildau.server.AmberServer;
import de.thwildau.stream.StreamManager;
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
			System.out.println(StreamManager.videoStreams.size());
			System.out.println(StreamManager.videoStreams.containsKey(vehicleID));
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
			vehicleID = (String) request[1];
			StreamManager.getStream(vehicleID).startRecord(vehicleID);
			break;
		case "stopRecord":
			vehicleID = (String) request[1];
			StreamManager.getStream(vehicleID).stopRecord(vehicleID);
			break;
		case "requestLogin":
			String[] loginContent = parseLoginRequest((JSONObject) request[1]);
			String usernameLogin = loginContent[0];
			byte[] passLogin = passwordToHash(loginContent[1]);
			// Check for User and Password
			int userID = AmberServer.getDatabase().adminLogin(usernameLogin, passLogin);
			// No match with username and password
			if(userID == -1){				
				responseMessage = new WebsocketResponse(WebsocketResponse.ERROR, Constants.ERROR_LOGIN).toJSON();
				ServerLogger.log("Web App Login failed - Wrong Username or Password: " + usernameLogin, Constants.DEBUG);
			}
			// User is not allowed to visit the web app
			else if (userID == -2){
				responseMessage = new WebsocketResponse(WebsocketResponse.ERROR, Constants.ERROR_ADMIN).toJSON();
				ServerLogger.log("Web App Login failed - No Admin: " + usernameLogin, Constants.DEBUG);
			}
			// Access granted
			else{				
				UserData response = new UserData();
				response = response.prepareUserData(userID);
				responseMessage = new WebsocketResponse(WebsocketResponse.LOGIN, response).toJSON();
				sessions.put(session, userID);
				ServerLogger.log("Web App Login success: " + usernameLogin, Constants.DEBUG);
			}
			session.getBasicRemote().sendText(responseMessage);
			break;
		case "requestLogout":
			userID = safeLongToInt((long)request[1]);
			AmberServer.getDatabase().logout(userID);
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


	private String[] parseCarsRequest(JSONObject jsonObject) {
		String[] request = null;

		if (jsonObject instanceof JSONObject) {
			request = new String[jsonObject.keySet().size()];
			request[0] = (String) jsonObject.get("vehicleID");
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