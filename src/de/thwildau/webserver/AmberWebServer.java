package de.thwildau.webserver;

import java.net.InetAddress;
import java.net.ServerSocket;

import javax.websocket.server.ServerContainer;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import de.thwildau.database.DatabaseAccess;
import de.thwildau.gcm.GCMNotification;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;
import de.thwildau.util.ServerPreferences;

public class AmberWebServer
{

	private static DatabaseAccess database;

	public static void init() {
		try {

			// Database Connection
			database = new DatabaseAccess();

			// Init Server
			Server server = new Server(Integer.parseInt(ServerPreferences.getProperty(Constants.WEB_PORT)));
			ServerLogger.log("Webserver " + InetAddress.getLocalHost().getHostAddress() + " listening on port: "+ ServerPreferences.getProperty(Constants.WEB_PORT), Constants.DEBUG);

			// Add Context
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");
			context.addServlet(new ServletHolder(new GCMNotification()),"/send");

			// Add website resources to web server
			ResourceHandler resource_handler = new ResourceHandler();
			resource_handler.setDirectoriesListed(false);
			resource_handler.setResourceBase("WEB-INF");
			resource_handler.setWelcomeFiles(new String[]{"index.html" });
			resource_handler.setHandler(new AmberWebServerHandler());

			// Manage handlers
			HandlerList handlers = new HandlerList();
			handlers.setHandlers(new Handler[] {resource_handler, context});
			server.setHandler(handlers);
			
			// Initialize websocket layer
			ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);

			// Add WebSocket endpoint to websocket layer
			wscontainer.addEndpoint(WebSocket.class);

			// Start Server
			server.start();
			//			server.join();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
