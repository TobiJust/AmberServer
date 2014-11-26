package de.thwildau.webserver;

import java.net.InetAddress;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import de.thwildau.database.DatabaseAccess;
import de.thwildau.gcm.GCMNotification;
import de.thwildau.server.AmberServerHandler;
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

			// Server
			Server server = new Server(Integer.parseInt(ServerPreferences.getProperty(Constants.WEB_PORT)));
			ServerLogger.log("Webserver " + InetAddress.getLocalHost().getHostAddress() + " listening on port: "+ ServerPreferences.getProperty(Constants.WEB_PORT), true);

			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");
			server.setHandler(context);

			context.addServlet(new ServletHolder(new GCMNotification()),"/send");

			ResourceHandler resource_handler = new ResourceHandler();
			resource_handler.setDirectoriesListed(false);
			resource_handler.setResourceBase("WEB-INF");
			resource_handler.setWelcomeFiles(new String[]{"index.html" });
			resource_handler.setHandler(new AmberWebServerHandler());

			HandlerList handlers = new HandlerList();
			handlers.setHandlers(new Handler[] { resource_handler});
			server.setHandler(handlers);

			server.start();
			//			server.join();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
