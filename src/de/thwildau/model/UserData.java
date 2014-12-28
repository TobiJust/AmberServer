package de.thwildau.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import de.thwildau.server.AmberServer;

public class UserData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 465312928422935320L;

	private ArrayList<Vehicle> vehicleList = new ArrayList<Vehicle>();
	private int userID;

	public UserData prepareUserData(int userID){
		ArrayList<Vehicle> vehicleList = AmberServer.getDatabase().getVehicles(userID);
		for(Vehicle vehicle : vehicleList){
			Object[] vehicleData = AmberServer.getDatabase().getEvents(vehicle.getVehicleID());
			ArrayList<Integer> eventIDList = (ArrayList<Integer>) vehicleData[1];
			vehicle.setVehicleName((String)vehicleData[0]);
			vehicle.setImage((byte[]) vehicleData[2]);
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
			vehicle.setVehicleID(vehicle.getVehicleID());
			this.vehicleList.add(vehicle);
		}
		this.userID = userID;
		return this;
	}

	public ArrayList<Vehicle> getVehicles(){
		return this.vehicleList;
	}

	public int getUserID() {
		return userID;
	}

}
