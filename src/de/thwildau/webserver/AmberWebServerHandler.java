package de.thwildau.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import de.thwildau.util.Constants;

public class AmberWebServerHandler extends AbstractHandler{
	
	/**
	 * Handle requests to the web server 
	 */
	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response)
			throws IOException, ServletException{

		// allow cross origin
		if(Constants.DEBUG)
			response.addHeader("Access-Control-Allow-Origin", "*");

		try{
			String requestIdent = request.getPathInfo().split("/")[1];
			switch(requestIdent){
			case "download":
				break;
			case "websocket":
				break;
			default:
				// No Access to those websites
				response.sendError(404);				
				break;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}