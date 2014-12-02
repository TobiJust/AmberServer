package de.thwildau.model;

import java.io.Serializable;
import java.util.ArrayList;

import de.thwildau.server.AmberServer;

public class UserData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 465312928422935320L;

	private ArrayList<Vehicle> vehicleList = new ArrayList<Vehicle>();
	private int userID;

	public UserData prepareUserData(int userID){
		ArrayList<Integer> vehicleIDList = AmberServer.getDatabase().getVehicles(userID);
		for(int vehicleID : vehicleIDList){
			Object[] vehicleData = AmberServer.getDatabase().getEvents(vehicleID);
			ArrayList<Integer> eventIDList = (ArrayList<Integer>) vehicleData[1];
			Vehicle vehicle = new Vehicle((String)vehicleData[0]);
			for(int eventID : eventIDList){
				Object[] eventData = AmberServer.getDatabase().getEventData(eventID);
				String eventType = (String)eventData[1];
				String eventTime = (String)eventData[2];
				double eventLat = (double)eventData[3];
				double eventLon = (double)eventData[4];
				byte[] eventImage = (byte[]) eventData[5];	// EventImage

				Event event = new Event(eventType, eventTime, eventLat, eventLon, eventImage);
				// Add events to the current Vehicle
				vehicle.getEventList().add(event);

			}
			// Add vehicles to the current User
			this.vehicleList.add(vehicle);
			this.userID = userID;
		}
		return this;
	}

	public ArrayList<Vehicle> getVehicles(){
		return this.vehicleList;
	}

}
