package de.thwildau.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import de.thwildau.server.AmberServer;

public class UserData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 465312928422935320L;

	private ArrayList<Vehicle> vehicleList = new ArrayList<Vehicle>();
	private ArrayList<String> vehicleNames = new ArrayList<String>();

	public UserData prepareUserData(int userID){
		ArrayList<Integer> vehicleIDList = AmberServer.getDatabase().getVehicles(userID);
		for(int vehicleID : vehicleIDList){
			Object[] vehicleData = AmberServer.getDatabase().getEvents(vehicleID);
			ArrayList<Integer> eventIDList = (ArrayList<Integer>) vehicleData[1];
			Vehicle vehicle = new Vehicle((String)vehicleData[0]);
			System.out.println((String)vehicleData[0]);
			for(int eventID : eventIDList){
				Object[] eventData = AmberServer.getDatabase().getEventData(eventID);
				System.out.println(eventData[0] + " " +eventData[1] +
						" " + eventData[2] + " " + eventData[3] +
						" " + eventData[4] + " " +eventData[5]);
				String eventType = (String)eventData[1];
				String eventTime = (String)eventData[2];
				double eventLat = (double)eventData[3];
				double eventLon = (double)eventData[4];
				byte[] eventImage = (byte[]) eventData[5];	// EventImagem

				Event event = new Event(eventType, eventTime, eventLat, eventLon, eventImage);
				// Add events to the current Ride
				vehicle.getEventList().add(event);

			}
			// Add vehicles to the current User
			this.vehicleList.add(vehicle);
		}
		return this;
	}

	private byte[] writeObject(BufferedImage image) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", baos);
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();
		
		return imageInByte;

	}

	public ArrayList<Vehicle> getVehicles(){
		return this.vehicleList;
	}

}
