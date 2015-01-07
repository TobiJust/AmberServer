package de.thwildau.gcm;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import de.thwildau.gcm.google.Message;
import de.thwildau.gcm.google.MulticastResult;
import de.thwildau.gcm.google.Sender;
import de.thwildau.server.AmberServer;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerPreferences;

public class SendNotification {
	// Put your Google API Server Key here
	private static final String GOOGLE_SERVER_KEY = ServerPreferences.getProperty(Constants.API_KEY);
	static final String MESSAGE_KEY = "message";

	public SendNotification(String type, String msg, String obuID){

		/**
		 *  Get valid GCM Registration IDs from the database.
		 *  Only if Users are logged in and are subscribers of a certain Vehicle,
		 *  the Registration ID will be added to the list of devices who will receive notifications.
		 */
		ArrayList<Integer> userList = AmberServer.getDatabase().getNotificationUsers(obuID);
		List<String> regIdList = new ArrayList<String>();
		for(int id : userList){
			regIdList.addAll(AmberServer.getDatabase().getGCMRegIds(id));
		}

		try	{
			//-------------------------------------
			BufferedImage img = ImageIO.read(new File("responseImage.jpg"));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write( img, "jpg", baos );
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();
			//-------------------------------------
			int eventID = AmberServer.getDatabase().addEvent(obuID, type, "12122014-112534", "52.13", "13.15", imageInByte);
			
			MulticastResult result = null;

			Sender sender = new Sender(GOOGLE_SERVER_KEY);
			Message message = new Message.Builder().timeToLive(30)
					.delayWhileIdle(true).addData(MESSAGE_KEY, msg).addData("type", type)
					.addData("eventID", ""+eventID).addData("obuID", obuID)
					.build();	
			result = sender.send(message, regIdList, 1);
		}
		catch (Exception e)
		{
			// deal with the exception in your "controller"
		}
	}

	/**
	 * Returns the output from the given URL.
	 * 
	 * I tried to hide some of the ugliness of the exception-handling
	 * in this method, and just return a high level Exception from here.
	 * Modify this behavior as desired.
	 * 
	 * @param desiredUrl
	 * @throws Exception
	 */
	private void doHttpUrlConnectionAction(String desiredUrl)
			throws Exception
	{
		URL url = null;
		BufferedReader reader = null;
		StringBuilder stringBuilder;

		try
		{
			// create the HttpURLConnection
			url = new URL(desiredUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// just want to do an HTTP GET here
			connection.setRequestMethod("GET");

			// uncomment this if you want to write output to this url
			//connection.setDoOutput(true);

			// give it 15 seconds to respond
			connection.setReadTimeout(15*1000);
			connection.connect();

			// read the output from the server
			connection.getInputStream();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			// close the reader; this can throw an exception too, so
			// wrap it in another try/catch block.
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		}
	}
}

