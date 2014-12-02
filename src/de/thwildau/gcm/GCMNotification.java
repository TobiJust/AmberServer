package de.thwildau.gcm;


import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.thwildau.gcm.google.Message;
import de.thwildau.gcm.google.MulticastResult;
import de.thwildau.gcm.google.Sender;
import de.thwildau.server.AmberServer;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerPreferences;

@WebServlet("/send")
public class GCMNotification extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Put your Google API Server Key here
	private static final String GOOGLE_SERVER_KEY = ServerPreferences.getProperty(Constants.API_KEY);
	static final String MESSAGE_KEY = "message";
	static final String REG_ID_STORE = "GCMRegId.txt";

	public GCMNotification() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		MulticastResult result = null;

		try {

			String userMessage = request.getParameter("message");
			Sender sender = new Sender(GOOGLE_SERVER_KEY);
			Message message = new Message.Builder().timeToLive(30)
					.delayWhileIdle(true).addData(MESSAGE_KEY, userMessage)
					.build();
			//				Set regIdSet = readFromFile();
			//				regIdList.addAll(regIdSet);
			List<String> regIdList = AmberServer.getDatabase().getGCMRegIds();

			System.out.println("regId: " + regIdList);
			System.out.println(message);			
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println("<h1>"+response.getStatus()+"</h1>");
			response.getWriter().println("session=" + request.getSession(true).getId());
			result = sender.send(message, regIdList, 1);	
			request.setAttribute("pushStatus", result.toString());

		} catch (IOException ioe) {
			ioe.printStackTrace();
			request.setAttribute("pushStatus",
					"RegId required: " + ioe.toString());
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("pushStatus", e.toString());
		}
	}
}
