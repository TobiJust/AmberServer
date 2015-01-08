package de.thwildau.gcm;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
	private  final String MESSAGE_KEY = "message";
	private static final String TYPE_KEY = "type";
	private static final String OBU_KEY = "obuID";
	private static final String EVENT_KEY = "eventID";

	public SendNotification(String type, String msg, int obuID, String lat, String lon){

		/**
		 *  Get valid GCM Registration IDs from the database.
		 *  Only if Users are logged in and are subscribers of a certain Vehicle,
		 *  the Registration ID will be added to the list of devices who will receive notifications.
		 */
		ArrayList<Integer> userList = AmberServer.getDatabase().getNotificationUsers(obuID);
		List<String> regIdList = new ArrayList<String>();
		for(int id : userList)
			regIdList.addAll(AmberServer.getDatabase().getGCMRegIds(id));

		try	{
			//-------------------------------------
			BufferedImage img = ImageIO.read(new File("responseImage.jpg"));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write( img, "jpg", baos );
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();
			//-------------------------------------
			int eventID = AmberServer.getDatabase().addEvent(obuID, type, "12122014-112534", lat, lon, imageInByte);
			
			Sender sender = new Sender(GOOGLE_SERVER_KEY);
			Message message = new Message.Builder().timeToLive(30)
					.delayWhileIdle(true).addData(MESSAGE_KEY, msg).addData(TYPE_KEY, type)
					.addData(EVENT_KEY, ""+eventID).addData(OBU_KEY, ""+obuID)
					.build();	
			MulticastResult result = sender.send(message, regIdList, 3);
		}
		catch (Exception e){
			// deal with the exception in your "controller"
		}
	}
}

