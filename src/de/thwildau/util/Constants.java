package de.thwildau.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

	public static final boolean DEBUG = true;

	public static final String PROPERTIES_FILENAME = "server.properties";
	public static final String PROPERTIES_PATH = "";

	public static final String PORT = "port";
	public static final String WEB_PORT = "web_port";

	public static final String API_KEY = "api_key";
	public static final String PROJECT_NR = "project_nr";

	public static final String SUCCESS_REGISTER = "Registration succeded";
	public static final String SUCCESS_REGISTER_VEHICLE = "Add Vehicle succeeded";
	public static final String SUCCESS_STREAM_STARTED = "Stream successful started";
	public static final String SUCCESS_STREAM_CLOSED = "Stream successful closed";

	public static final String ERROR_LOGIN = "Wrong Username or Password!";
	public static final String ERROR_ADMIN = "Wrong Username or Password!";
	public static final String ERROR_GCM = "Error while registering GCM";
	public static final String ERROR_REGISTER = "Registration failed";	
	public static final String ERROR_RECORD = "Error while recording";
	public static final String ERROR_REGISTER_VEHICLE = "Can't add new Vehicle";
	public static final String ERROR_UNREGISTER_VEHICLE = "Can't unregister Vehicle";
	public static final String ERROR_UNKNOWN = "Unknown argument given";


	public static final String ARG_GCM_SEND = "send";	
	public static final String ARG_MESSAGE = "message";
	public static final String ARG_USERID = "userID";
	public static final String ARG_TYPE = "type";
	public static final String ARG_PORT = "p";
	public static final String ARG_DEFAULT = "defaults";	

	public static final String DATA_FOLDER = "data/";


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