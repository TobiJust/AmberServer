package de.thwildau.server;

/**
 * @author Just
 */
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import de.thwildau.database.DatabaseAccess;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;
import de.thwildau.util.ServerPreferences;

public class AmberServer{

	private static NioSocketAcceptor acceptor;
	private static DatabaseAccess database;

	public static NioSocketAcceptor getAcceptor(){
		return acceptor;
	}
	public static DatabaseAccess getDatabase() {
		return database;
	}

	public static void init(){

		acceptor = new NioSocketAcceptor();
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ServerCodecFactory()));
//		acceptor.getFilterChain().addLast("protocol",
//				new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
		System.out.println(acceptor.getFilterChain().getAll());

		try{
			//Database Connection
			database = new DatabaseAccess();

			acceptor.setHandler(AmberServerHandler.getInstance());
			acceptor.bind(new InetSocketAddress(Integer.parseInt(ServerPreferences.getProperty(Constants.PORT))));
			ServerLogger.log("Server " + InetAddress.getLocalHost().getHostAddress() + " listening on port: "+ ServerPreferences.getProperty(Constants.PORT), true);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void stop(){
		getDatabase().close();
	}


}