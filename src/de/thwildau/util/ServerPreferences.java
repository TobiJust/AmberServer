package de.thwildau.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerPreferences {

	private static Properties properties;

	public static void setProperties(Properties prop){
		properties = prop;
	}

	public static String getProperty(String key){
		return properties.getProperty(key);
	}

	public static void setProperty(String key, String value){
		properties.setProperty(key, value);
	}

	public static void storeProperties(){
		BufferedOutputStream stream;
		try{
			stream = new BufferedOutputStream(new FileOutputStream("server.properties"));
			properties.store(stream, Constants.PROPERTIES_PATH+Constants.PROPERTIES_FILENAME);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void restoreDefaults(){
		properties = new Properties();
		properties.setProperty("port", Constants.PORT);
	}

	public static void loadProperties(){
		properties = new Properties();
		BufferedInputStream stream;
		try {
			stream = new BufferedInputStream(new FileInputStream(Constants.PROPERTIES_PATH+Constants.PROPERTIES_FILENAME));
			properties.load(stream);
			stream.close();
			ServerLogger.log("Properties loaded...", true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}