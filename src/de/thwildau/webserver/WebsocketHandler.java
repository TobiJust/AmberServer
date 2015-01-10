package de.thwildau.webserver;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.thwildau.util.Util;

public class WebsocketHandler{
	
	/**
	 * 
	 * @param message
	 * @return
	 */
	public static Object[] parseRequest(String message){
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
	public static String[] parseLoginRequest(JSONObject jsonObject) {
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
	public static int[] parseCarsRequest(JSONObject jsonObject) {
		int[] request = null;

		if (jsonObject instanceof JSONObject) {
			request = new int[jsonObject.keySet().size()];
			request[0] = Util.safeLongToInt((long)jsonObject.get("vehicleID"));
		}
		return request;
	}	
	/**
	 * 
	 * @param jsonObject
	 * @return
	 */
	public static Object[] parseCommandRequest(JSONObject jsonObject) {
		Object[] request = null;

		if (jsonObject instanceof JSONObject) {
			request = new Object[jsonObject.keySet().size()];
			request[0] = Util.safeLongToInt((long)jsonObject.get("carID"));
			request[1] = (String)jsonObject.get("command");
		}
		return request;
	}	
	
	public static Object[] parseStreamRequest(JSONObject jsonObject) {
		Object[] request = null;

		if (jsonObject instanceof JSONObject) {
			request = new Object[2];
			request[0] = Util.safeLongToInt((long)jsonObject.get("carID"));
			request[1] = Util.safeLongToInt((long)jsonObject.get("userID"));
		}
		return request;
	}	
	/**
	 * 
	 * @param jsonObject
	 * @return
	 */
	public static Object[] parseRecordRequest(JSONObject jsonObject) {
		Object[] request = null;

		if (jsonObject instanceof JSONObject) {
			request = new Object[2];
			request[0] = Util.safeLongToInt((long)jsonObject.get("carID"));
			request[1] = Util.safeLongToInt((long)jsonObject.get("userID"));
		}
		return request;
	}	
}