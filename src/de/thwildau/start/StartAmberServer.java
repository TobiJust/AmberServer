package de.thwildau.start;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.sarxos.webcam.Webcam;

import de.thwildau.database.DatabaseAccess;
import de.thwildau.gcm.SendNotification;
import de.thwildau.info.OBUMessage;
import de.thwildau.obu.OBUResponseHandler;
import de.thwildau.server.AmberServer;
import de.thwildau.server.AmberServerHandler;
import de.thwildau.stream.StreamManager;
import de.thwildau.stream.VideoStreamer;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;
import de.thwildau.util.ServerPreferences;
import de.thwildau.webserver.AmberWebServer;


public class StartAmberServer {

	private static boolean quit = false;
	private static String[] arguments;
	private static OBUResponseHandler orh = new OBUResponseHandler();
	protected static boolean running = true;

	public static void main(String[] args){
		arguments = args;		

		initLogger();
		AmberServer.init();
		AmberWebServer.init();

		StreamManager.addStream("BMW_I8", new VideoStreamer());
		
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
					new SendNotification("Oil", "Your_Oil", "CIT_C4");
					ServerLogger.log("Send Notification", true);
					break;
				case "send1":
					new SendNotification("Temperature", "Your_Temperature", "CIT_C4");
					ServerLogger.log("Send Notification", true);
					break;
				case "send2":
					new SendNotification("Bend", "Bend radius is too small", "BMW_I8");
					ServerLogger.log("Send Notification", true);
					break;
				case "send3":
					new SendNotification("Accident", "Crash", "AUD_A8");
					ServerLogger.log("Send Notification", true);
					break;
				case "obu":
					byte[] b = {(byte)0x01};
					OBUResponseHandler.handlers.get("123").write(new OBUMessage(OBUMessage.REQUEST_TELEMETRY, b).request);
					break;
				case "obuDebug":
//					final boolean running = true;
					Runnable run = new Runnable() {
						public void run() {
							try {
								for (int i = 0; i < 20; i++) {
									Thread.sleep(1000);
								}
								running = false;
							} catch (InterruptedException e) {
								System.out.println(" interrupted");
							}
						}
					};
					new Thread(run).start();
					StreamManager.getStream("BMW_I8").startStream();
//					StreamManager.getStream("BMW_I8").startRecord("BMW_I8");
					try {
						while(running){
							boolean transactionState = orh.addData(Constants.sendData());
						}
//						StreamManager.getStream("BMW_I8").stopRecord("BMW_I8");
						StreamManager.getStream("BMW_I8").stopStream();
					} catch (Exception e) {
						e.printStackTrace();
					}

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