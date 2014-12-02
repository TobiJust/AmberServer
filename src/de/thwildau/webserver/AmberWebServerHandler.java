package de.thwildau.webserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.thwildau.model.UserData;
import de.thwildau.server.AmberServer;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;

public class AmberWebServerHandler extends AbstractHandler{

	private static final boolean DEBUG = true;

	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response)
			throws IOException, ServletException{

		// allow cross origin
		response.addHeader("Access-Control-Allow-Origin", "*");
		try{
			String requestIdent = request.getPathInfo();
			switch(requestIdent){
			case "/login":
				String loginData = request.getParameter("data");
				String usernameLogin = parseLoginRequest(loginData)[0];
				//TODO: get hash value from Web App
				byte[] passLogin = passwordToHash(parseLoginRequest(loginData)[1]);
				// Database validation
				// Check for User and Password
				int user_id = AmberServer.getDatabase().login(usernameLogin, passLogin);
				if(user_id == -1){
					response.sendError(500, Constants.ERROR_LOGIN);
					ServerLogger.log("Login from Web App failed: " + usernameLogin, DEBUG);
				}
				// Everything ok - Login succeeded
				else{
					UserData responseData = new UserData();
					responseData = responseData.prepareUserData(user_id);
					response.setHeader("Content-Type", "text/plain");
					PrintWriter writer = response.getWriter();
					writer.write(convertToJSON(responseData));
					writer.close();
					ServerLogger.log("Login from Web App succeeded: " + usernameLogin, DEBUG);
				}
				break;
			case "/logout":
				String logoutData = request.getParameter("data");
				String usernameLogout = parseLoginRequest(logoutData)[0];
				// Database validation
				// Check for User and Password
				//				user_id = AmberServer.getDatabase().logout(usernameLogout);
				ServerLogger.log("Logout from Web App succeeded", DEBUG);
				break;
			case "/requestDataPackage":
				int userID = Integer.parseInt(request.getParameter("userID"));
				UserData responseData = new UserData();
				responseData = responseData.prepareUserData(userID);
				if(true)
					response.sendError(500, Constants.ERROR_RECORD);
				else{
					response.setHeader("Content-Type", "text/plain");
					PrintWriter writer = response.getWriter();
					writer.write(convertToJSON(responseData));
					writer.close();
					ServerLogger.log("Request from Web App", DEBUG);
				}
				break;
			case "/send":
				ServerLogger.log("Send Notification to Devices", DEBUG);
				break;
			case "/startRecord":
				userID = Integer.parseInt(request.getParameter("userID"));
				int vehicleID = Integer.parseInt(request.getParameter("vehicleID"));
				int cameraID = Integer.parseInt(request.getParameter("cameraID"));	
				response.setHeader("Content-Type", "text/plain");
				PrintWriter writer = response.getWriter();
				writer.write("Starting to record from camera " + cameraID);
				writer.close();
				ServerLogger.log("Starting to record from camera " + cameraID + " on Vehicle "
						+ vehicleID + " for User " + userID, DEBUG);
				break;
			case "/stopRecord":
				ServerLogger.log("Video recorded and processed, ready to download.", DEBUG);
				break;
			case "/sendCommand":
				break;
			default:
				// No Access to those websites
				response.sendError(404);				
				break;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Parse incoming data in JSON Format to Strings.
	 * 
	 * @param data JSON String received from Web App
	 * @return
	 */
	private String[] parseLoginRequest(String requestData){
		String[] login = null;
		JSONParser parser = new JSONParser();
		Object resultObject;
		try {
			resultObject = parser.parse(requestData);

			if (resultObject instanceof JSONArray) {
				JSONArray array=(JSONArray)resultObject;
				for (Object object : array) {
					JSONObject obj =(JSONObject)object;
					login = new String[obj.keySet().size()];
					login[0] = (String) obj.get("email");
					login[1] = (String) obj.get("password");
				}

			}else if (resultObject instanceof JSONObject) {
				JSONObject obj = (JSONObject)resultObject;
				login = new String[obj.keySet().size()];
				login[0] = (String) obj.get("email");
				login[1] = (String) obj.get("password");
			}	
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return login;
	}
	private String convertToJSON(Object responseData){
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = null;
		try {
			json = ow.writeValueAsString(responseData);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;			
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
}