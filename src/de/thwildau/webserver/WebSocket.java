package de.thwildau.webserver;
import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import xuggler.DesktopStream;
import de.thwildau.stream.VideoStream;
import de.thwildau.util.ServerLogger;

@ServerEndpoint("/websocket/")
public class WebSocket {

	private static final boolean DEBUG = true;
	private VideoStream stream = new VideoStream(0);	
	private BufferedOutputStream bos = null;


	@OnMessage
	public void onMessage(String message, Session session) 
			throws IOException, InterruptedException {

		// Send the first message to the client
		session.getBasicRemote().sendText("This is the first server message");
		
		String ident = message.split(";")[0];
		String content = (message.split(";").length > 1) ? message.split(";")[1] : "No Content";
		switch(ident){
		case "stream":
			ServerLogger.log(content, DEBUG);
			new DesktopStream(session);
			break;
		case "startStream":
			ServerLogger.log(content, DEBUG);
			break;
		case "endStream":
			ServerLogger.log(content, DEBUG);
			break;
		default:
			break;
		}

		// Send a final message to the client
		session.getBasicRemote().sendText("This is the last server message");
	}

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("Client connected " + session.getId());
	}

	@OnClose
	public void onClose() {
		System.out.println("Connection closed");
	}
}