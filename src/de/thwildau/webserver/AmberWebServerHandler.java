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

	/**
	 * Handle requests to the web server 
	 */
	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response)
			throws IOException, ServletException{

		// allow cross origin
		if(Constants.DEBUG)
			response.addHeader("Access-Control-Allow-Origin", "*");

		try{
			String requestIdent = request.getPathInfo().split("/")[1];

			switch(requestIdent){
			case "send":
				break;
			case "websocket":
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