package de.thwildau.server;

/**
 * @author Just
 */
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import de.thwildau.database.DatabaseManager;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerCodecFactory;
import de.thwildau.util.ServerLogger;
import de.thwildau.util.ServerPreferences;

public class AmberServer{

	private static NioSocketAcceptor acceptor;
	private static DatabaseManager database;

	public static NioSocketAcceptor getAcceptor(){
		return acceptor;
	}
	public static DatabaseManager getDatabase() {
		return database;
	}
	/**
	 * 
	 */
	public static void init(){

		acceptor = new NioSocketAcceptor();
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ServerCodecFactory()));

		try{
			//Database Connection
			database = new DatabaseManager();
			acceptor.setReuseAddress(true);
			acceptor.setHandler(AmberServerHandler.getInstance());
			acceptor.bind(new InetSocketAddress(Integer.parseInt(ServerPreferences.getProperty(Constants.PORT))));
			ServerLogger.log("Server " + InetAddress.getLocalHost().getHostAddress() + " listening on port: "+ ServerPreferences.getProperty(Constants.PORT), Constants.DEBUG);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void stop(){
		getDatabase().close(null);
	}


}