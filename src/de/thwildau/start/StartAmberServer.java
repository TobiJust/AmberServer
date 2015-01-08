package de.thwildau.start;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.thwildau.gcm.SendNotification;
import de.thwildau.info.OBUMessage;
import de.thwildau.obu.OBUResponseHandler;
import de.thwildau.server.AmberServer;
import de.thwildau.server.AmberServerHandler;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;
import de.thwildau.util.ServerPreferences;
import de.thwildau.util.Util;
import de.thwildau.webserver.AmberWebServer;


public class StartAmberServer {

	private static boolean quit = false;
	private static String[] arguments;
	private static OBUResponseHandler orh = new OBUResponseHandler();
	protected static boolean running = true;
	static int i = 0;

	public static void main(String[] args){
		arguments = args;		

		initLogger();
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
					ServerLogger.log("Loading default properties...", Constants.DEBUG);
					ServerPreferences.restoreDefaults();
					ServerPreferences.storeProperties();
					restart();
					break;
				case "restart":
					restart();
					break;
				case "add user":
					Util.addUser();
					break;
				case "show user":
					ServerLogger.log(AmberServer.getDatabase().showAllUser(), Constants.DEBUG);
					break;
				case "add vehicle":
					Util.addVehicle();
					break;
				case "log on":
					Constants.DEBUG = true;
					break;
				case "log off":
					Constants.DEBUG = true;
					break;
				case "send":
					new SendNotification("Oil", "Your_Oil", 1, "52.14", "13.37");
					ServerLogger.log("Send Notification", Constants.DEBUG);
					break;
				case "send1":
					new SendNotification("Temperature", "Your_Temperature", 1, "52.14", "13.37");
					ServerLogger.log("Send Notification", Constants.DEBUG);
					break;
				case "send2":
					new SendNotification("Bend", "Bend radius is too small", 5, "52.14", "13.37");
					ServerLogger.log("Send Notification", Constants.DEBUG);
					break;
				case "send3":
					new SendNotification("Accident", "Crash", 2, "52.14", "13.37");
					ServerLogger.log("Send Notification", Constants.DEBUG);
					break;
				case "obu":
					byte[] b = {(byte)0x01};
					OBUResponseHandler.handlers.get(123).write(new OBUMessage(OBUMessage.REQUEST_TELEMETRY, b).request);
					break;
				case "obuDebug":
					System.out.println("Start OBU Stream");
					Runnable run = new Runnable() {
						public void run() {
							try {
								if(running)
									for (i = 0; i < 150; i++) {
										Thread.sleep(100);
										System.out.print(".");
									}
								
								running = false;
							} catch (InterruptedException e) {
								System.out.println(" interrupted");
							}
						}
					};
					new Thread(run).start();
					Runnable obu = new Runnable() {
						public void run() {
							while(running){
								try {
									orh.addData(Constants.sendData());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							System.out.println("Stream beendet for Debugging");
							running = true;
							i = 0;						
						}
					};
					new Thread(obu).start();

					break;
				default:
					ServerLogger.log("Command not found", Constants.DEBUG);
					break;
				}
			} catch (IOException e) {
				System.err.println("Exception caught in command line. Shutting down...");
				shutdown();
			}
		}
	}

	private static void initLogger(){
		try {
			ServerLogger.init(true);
			ServerPreferences.loadProperties();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	private static void restart(){
		ServerLogger.log("Restarting serverthread...", true);
		ServerLogger.log("Stopping serverthread...", true);
		AmberServer.getAcceptor().dispose();
		AmberServerHandler.clearInstance();
		AmberServer.stop();
		AmberServer.init();
	}

	private static void shutdown(){
		AmberServer.getAcceptor().dispose();
		ServerLogger.stop(true);
		ServerPreferences.storeProperties();
		quit = true;
	}
}