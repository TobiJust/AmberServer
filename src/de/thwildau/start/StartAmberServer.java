package de.thwildau.start;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.thwildau.gcm.SendNotification;
import de.thwildau.server.AmberServer;
import de.thwildau.server.AmberServerHandler;
import de.thwildau.util.ServerLogger;
import de.thwildau.util.ServerPreferences;
import de.thwildau.webserver.AmberWebServer;


public class StartAmberServer {

	private static boolean quit = false;
	private static String[] arguments;

	public static void main(String[] args){

		arguments = args;		

		try {
			ServerLogger.init(true);
			ServerPreferences.loadProperties();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		AmberServer.init();
		AmberWebServer.init();

		BufferedReader din = new BufferedReader(new InputStreamReader(System.in));
		while(!quit){
			System.out.print("> ");
			try {
				switch(din.readLine()){
				case "quit":
				case "exit":
					System.out.println("Shutting down...");
					shutdown();
					break;
				case "defaults":
					ServerLogger.log("Loading default properties...", true);
					ServerPreferences.restoreDefaults();
					ServerPreferences.storeProperties();
					restart();
					break;
				case "restart":
					restart();
					break;
				case "show user":
					ServerLogger.log(AmberServer.getDatabase().showAllUser(), true);
					break;
				case "send":
					new SendNotification("GCM_Notification");
					ServerLogger.log(AmberServer.getDatabase().getGCMRegIds().toString(), true);
					ServerLogger.log("Send Notification", true);
					break;
				}
			} catch (IOException e) {
				System.err.println("Exception caught in command line. Shutting down...");
				shutdown();
			}
		}
	}

	private static void restart(){
		ServerLogger.log("Restarting serverthread...", true);
		ServerLogger.log("Stopping serverthread...", true);
		AmberServer.getAcceptor().dispose();
		AmberServerHandler.clearInstance();
		AmberServer.stop();
		ServerLogger.stop(true);
		AmberServer.init();
	}

	private static void shutdown(){
		AmberServer.getAcceptor().dispose();
		ServerLogger.stop(true);
		ServerPreferences.storeProperties();
		quit = true;
	}
}