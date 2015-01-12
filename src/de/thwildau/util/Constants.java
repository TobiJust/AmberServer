package de.thwildau.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Constants used on Amber Server.
 * @author Tobias Just
 *
 */
public class Constants {

	/**
	 * Constant for debugging outputs.
	 */
	public static boolean DEBUG = true;
	public static boolean LOG = true;
	public static int LOG_INTERVAL = 1000;
	/**
	 * Properties 
	 */
	public static final String PROPERTIES_FILENAME = "server.properties";
	public static final String PROPERTIES_PATH = "";
	/**
	 * Constants used for the server ports.
	 */
	public static final String PORT = "port";
	public static final String WEB_PORT = "web_port";
	/**
	 * Constants used on GCM service communication.
	 */
	public static final String API_KEY = "api_key";
	public static final String PROJECT_NR = "project_nr";

	/**
	 * Constant strings to report a successful operation.
	 */
	public static final String SUCCESS_REGISTER = "Registration succeeded";
	public static final String SUCCESS_REGISTER_VEHICLE = "Register Vehicle succeeded";
	public static final String SUCCESS_STREAM_STARTED = "Stream successful started";
	public static final String SUCCESS_STREAM_CLOSED = "Stream successful closed";
	public static final String SUCCESS_LOGOUT = "Logout succeeded";
	public static final String SUCCESS_ADD_VEHICLE = "Add Vehicle suceeded";
	public static final String SUCCESS_SCREENSHOT = "Screenshot current stream";
	/**
	 * Constant strings to report a failed operation.
	 */
	public static final String ERROR_LOGIN = "Login failed";
	public static final String ERROR_ADMIN = "Login failed";
	public static final String ERROR_GCM = "Error while registering GCM";
	public static final String ERROR_REGISTER = "Registration failed";	
	public static final String ERROR_RECORD = "Error while recording";
	public static final String ERROR_REGISTER_VEHICLE = "Can't add new Vehicle";
	public static final String ERROR_UNREGISTER_VEHICLE = "Can't unregister Vehicle";
	public static final String ERROR_ADD_VEHICLE = "Failed to add vehicle";
	public static final String ERROR_LOGOUT = "Logout failed";
	public static final String ERROR_UNKNOWN = "Unknown argument given";
	
	/**
	 * Bend events
	 */
	public static final String TURN_EVENT 		= "Bend radius is too small";
	public static final String RPM_EVENT 		= "RPM is too high";
	public static final String ACC_EVENT 		= "Acceleration is not normal";
	public static final String SPEED_EVENT 		= "You are over the speed limit";
	public static final String FUEL_EVENT 		= "Fuel level is low";
	public static final String COOLANT_EVENT 	= "Coolant Temperature too high";
	public static final String GYRO_EVENT 		= "Gyro Event";
	public static final String DEFAULT_EVENT 	= "Default Warning";

	public static final String ARG_GCM_SEND = "send";	
	public static final String ARG_MESSAGE = "message";
	public static final String ARG_USERID = "userID";
	public static final String ARG_TYPE = "type";
	public static final String ARG_PORT = "p";
	public static final String ARG_DEFAULT = "defaults";	

	public static final String DATA_FOLDER = "datastore/";
	public static final String LOG_FOLDER = "log/";


	public static byte[] sendData(){
		byte[] data = null;
		try {
			if(index > 39)
				index = 0;
			Path path = Paths.get("data/danielData"+index);
			data = Files.readAllBytes(path);
			index++;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}


	private static int index = 0;



}