package de.thwildau.webserver;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.mina.core.session.IoSession;
import org.json.simple.JSONObject;

import de.thwildau.info.OBUMessage;
import de.thwildau.model.UserData;
import de.thwildau.model.Vehicle;
import de.thwildau.obu.OBUFrameHandler;
import de.thwildau.server.AmberServer;
import de.thwildau.stream.StreamManager;
import de.thwildau.stream.VideoStreamer;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;
import de.thwildau.util.Util;

@ServerEndpoint("/websocket/")
public class Websocket {

	public static ConcurrentHashMap<Session, Integer> sessions = new ConcurrentHashMap<Session, Integer>();

	/**
	 * 
	 * @param message
	 * @param session
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@OnMessage
	public void onMessage(String message, Session session) 
			throws IOException, InterruptedException {

		Runnable run = new Runnable() {

			@Override
			public void run() {
				Object[] request = WebsocketHandler.parseRequest(message);
				String responseMessage = null;
				String ident = (String) request[0];
				switch(ident){
				case "startStream":
					Object[] streamContent = WebsocketHandler.parseStreamRequest((JSONObject) request[1]);
					int vehicleID = (int)streamContent[0];
					int userID = (int) streamContent[1];
					VideoStreamer streamer = StreamManager.getStream(vehicleID);
					if(streamer == null){
						streamer = new VideoStreamer();
						StreamManager.addStream(vehicleID, streamer);
					}
					sessions.put(session, userID);
					streamer.startStream(session);
					responseMessage = new WebsocketResponse(WebsocketResponse.STREAM_STARTED, Constants.SUCCESS_STREAM_STARTED).toJSON();		
					break;
				case "stopStream":
					Object[] streamContent2 = WebsocketHandler.parseStreamRequest((JSONObject) request[1]);
					vehicleID = (int)streamContent2[0];
					userID = (int) streamContent2[1];
					StreamManager.getStream(vehicleID).stopStream();
					responseMessage = new WebsocketResponse(WebsocketResponse.STREAM_CLOSED, Constants.SUCCESS_STREAM_CLOSED).toJSON();
					session.getAsyncRemote().sendText(responseMessage);
					break;
				case "startRecord":
					Object[] recordContent = WebsocketHandler.parseRecordRequest((JSONObject) request[1]);
					vehicleID = (int)recordContent[0];
					userID = (int) recordContent[1];
					streamer = StreamManager.getStream(vehicleID);
					System.out.println(vehicleID);
					System.out.println(StreamManager.videoStreams.keySet());
					System.out.println("Streamer " + streamer);
					if(streamer != null)
						streamer.startRecord(userID, vehicleID);		
					break;
				case "stopRecord":
					recordContent = WebsocketHandler.parseRecordRequest((JSONObject) request[1]);
					vehicleID = (int) recordContent[0];
					userID = (int) recordContent[1];
					streamer = StreamManager.getStream(vehicleID);
					if(streamer != null)
						streamer.stopRecord(vehicleID);
					responseMessage = new WebsocketResponse(WebsocketResponse.STOP_RECORD, Constants.SUCCESS_STREAM_CLOSED).toJSON();
					//					Thread.sleep(100);
					session.getAsyncRemote().sendText(responseMessage);	
					break;
				case "requestLogin":
					Object[] loginContent = WebsocketHandler.parseLoginRequest((JSONObject) request[1]);
					String usernameLogin = (String)loginContent[0];
					byte[] passLogin = Util.passwordToHash((String)loginContent[1]);
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
						response = response.prepareAdminData(user_id);
						responseMessage = new WebsocketResponse(WebsocketResponse.LOGIN, response).toJSON();
						ServerLogger.log("Web App Login success: " + usernameLogin, Constants.DEBUG);
					}
					session.getAsyncRemote().sendText(responseMessage);
					break;
				case "requestLogout":
					user_id = Util.safeLongToInt((long)request[1]);
					AmberServer.getDatabase().logout(user_id);
					responseMessage = new WebsocketResponse(WebsocketResponse.LOGOUT, "Logout succeeded").toJSON();
					session.getAsyncRemote().sendText(responseMessage);
					break;
				case "requestCars":
					user_id = Util.safeLongToInt((long)request[1]);
					UserData response = new UserData();
					response = response.prepareUserData(user_id);
					responseMessage = new WebsocketResponse(WebsocketResponse.CARS_RESPONSE, response).toJSON();
					System.out.println(responseMessage);
					session.getAsyncRemote().sendText(responseMessage);
					ServerLogger.log("Return list of cars: " + user_id, Constants.DEBUG);
					break;
				case "sendCommand":
					Object[] commandContent = WebsocketHandler.parseCommandRequest((JSONObject)request[1]);
					String command = (String)commandContent[0];
//					int obuID = (int)commandContent[1];
					//					IoSession obuSession = OBUFrameHandler.handlers.get(obuID);
					IoSession obuSession = OBUFrameHandler.handlers.get(5);
					if(obuSession != null)
						switch(command){
						case "imageSwap":
							obuSession.write(new OBUMessage(OBUMessage.ID_COMMAND, OBUMessage.REQUEST_SWAP).request);
							System.out.println("WRITE IMAGE SWAP");
							System.out.println("WRITE IMAGE SWAP");
							System.out.println("WRITE IMAGE SWAP");
							break;
						case "dataRequest":				
							obuSession.write(new OBUMessage(OBUMessage.ID_COMMAND, OBUMessage.REQUEST_DATA).request);
							break;
						default:
							break;
						}
					break;
				case "addCar":
					Object[] carContent = WebsocketHandler.parseRecordRequest((JSONObject) request[1]);
					vehicleID = (int)carContent[0];
					userID = (int) carContent[1];

					Vehicle vehicle = AmberServer.getDatabase().registerVehicle(userID, vehicleID);
					if(vehicle == null){
						responseMessage = new WebsocketResponse(WebsocketResponse.ERROR, Constants.ERROR_REGISTER_VEHICLE).toJSON();
						ServerLogger.log("Add Vehicle failed: " + userID, Constants.DEBUG);
					}
					// Add Vehicle succeeded
					else{
						vehicle = vehicle.prepareVehicle(vehicleID);
						responseMessage = new WebsocketResponse(WebsocketResponse.REGISTER_VEHICLE, Constants.SUCCESS_REGISTER_VEHICLE).toJSON();
						ServerLogger.log("Add Vehicle succeeded: " + userID, Constants.DEBUG);				
					}
					session.getAsyncRemote().sendText(responseMessage);	
					break;
				case "requestScreenshot":
					recordContent = WebsocketHandler.parseRecordRequest((JSONObject) request[1]);
					vehicleID = (int) recordContent[0];
					userID = (int) recordContent[1];

					streamer = StreamManager.getStream(vehicleID);
					if(streamer != null)
						streamer.screenshot();

					responseMessage = new WebsocketResponse(WebsocketResponse.SCREENSHOT, Constants.SUCCESS_SCREENSHOT).toJSON();
					System.out.println(responseMessage);
					session.getAsyncRemote().sendText(responseMessage);	
					break;
				default:
					break;
				}
			}
		};
		new Thread(run).start();
	}
	@OnOpen
	public void onOpen(Session session) {
		ServerLogger.log("Web App Client connected: " + session.getId(), Constants.DEBUG);
	}

	@OnClose
	public void onClose(Session session) {
		sessions.remove(session);
		ServerLogger.log("Wep App Connection closed: " + session.getId(), Constants.DEBUG);
	}
}