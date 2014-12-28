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


	@OnMessage
	public void onMessage(String message, Session session) 
			throws IOException, InterruptedException {

		Object[] request = parseRequest(message);

		String responseMessage = null;
		String ident = (String) request[0];
		switch(ident){
		case "startStream":
			String vehicleID = (String) request[1];
			DesktopStream stream = new DesktopStream(session, vehicleID);
			stream.startStream();
			streams.put(session, stream);
			break;
		case "stopStream":
			vehicleID = (String) request[1];
			streams.get(session).stopStream();
			responseMessage = toJSON(new WebsocketResponse(WebsocketResponse.STREAM_CLOSED, "Stream closed"));
			break;
		case "startRecord":
			break;
		case "stopRecord":
			break;
		case "requestLogin":
			String[] loginContent = parseLoginRequest((JSONObject) request[1]);
			String usernameLogin = loginContent[0];
			byte[] passLogin = passwordToHash(loginContent[1]);
			// Check for User and Password
			int userID = AmberServer.getDatabase().login(usernameLogin, passLogin);
			if(userID == -1){				
				responseMessage = toJSON(new WebsocketResponse(WebsocketResponse.ERROR, "Login failed"));
				ServerLogger.log("Login failed: " + usernameLogin, Constants.DEBUG);
			}
			else{				
				UserData response = new UserData();
				response = response.prepareUserData(userID);
				responseMessage = toJSON(new WebsocketResponse(WebsocketResponse.LOGIN, response));
				ServerLogger.log("Login success: " + usernameLogin, Constants.DEBUG);
			}
			session.getBasicRemote().sendText(responseMessage);
			break;
		case "requestLogout":
			userID = safeLongToInt((long)request[1]);
			AmberServer.getDatabase().logout(userID);
			logoutUser(session);
			responseMessage = toJSON(new WebsocketResponse(WebsocketResponse.LOGOUT, "Logout succeeded"));
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
		System.out.println("Client connected " + session.getId());
	}

	@OnClose
	public void onClose(Session session) {
		streams.get(session).stopStream();
		System.out.println("Connection closed " + session.getId());
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
	public String toJSON(WebsocketResponse response){
		ObjectMapper mapper = new ObjectMapper();

		String json = null;
		try {
			json = mapper.writeValueAsString(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(json);
		return json;			
	}
	private void logoutUser(Session session) {
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