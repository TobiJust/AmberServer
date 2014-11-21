package de.thwildau.webserver;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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

			// Server
			Server server = new Server(Integer.parseInt(ServerPreferences.getProperty(Constants.WEB_PORT)));
			ServerLogger.log("Webserver " + InetAddress.getLocalHost().getHostAddress() + " listening on port: "+ ServerPreferences.getProperty(Constants.WEB_PORT), true);

			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");
			server.setHandler(context);

			context.addServlet(new ServletHolder(new AmberServlet("Buongiorno Mondo")),"/it/*");
			context.addServlet(new ServletHolder(new LoginServlet()),"/login");
			context.addServlet(new ServletHolder(new GCMNotification()),"/send");

			server.start();
//			server.join();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
