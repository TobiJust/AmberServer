package de.thwildau.model;

import java.io.Serializable;
import java.util.ArrayList;

import de.thwildau.server.AmberServer;

/**
 * 
 * @author Tobias Just
 *
 */
public class UserData implements Serializable{

	/**
	 * Serial ID for Serialization
	 */
	private static final long serialVersionUID = 465312928422935320L;

	/**
	 * List of vehicles per user
	 */
	private ArrayList<Vehicle> vehicleList = new ArrayList<Vehicle>();
	/**
	 * ID of the current user
	 */
	private int userID;

	/**
	 * 
	 * @param userID
	 * @return
	 */
	public UserData prepareUserData(int userID){
		ArrayList<Vehicle> vehicleList = AmberServer.getDatabase().getVehicles(userID);
		for(Vehicle vehicle : vehicleList){
			Object[] vehicleData = AmberServer.getDatabase().getEvents(vehicle.getVehicleID());
			vehicle.setVehicleName((String)vehicleData[0]);
			vehicle.setImage((byte[]) vehicleData[2]);
			// Add vehicles to the current User
			vehicle.setVehicleID(vehicle.getVehicleID());
			this.vehicleList.add(vehicle);
		}
		this.userID = userID;
		return this;
	}
	/**
	 * 
	 * @param adminID
	 * @return
	 */
	public UserData prepareAdminData(int adminID){
		ArrayList<Vehicle> vehicleList = AmberServer.getDatabase().getVehicles(adminID);
		for(Vehicle vehicle : vehicleList){
			Object[] vehicleData = AmberServer.getDatabase().getEvents(vehicle.getVehicleID());
			ArrayList<Integer> eventIDList = (ArrayList<Integer>) vehicleData[1];
			String vehicleName = (String)vehicleData[0];
			vehicle.setImage((byte[]) vehicleData[2]);
			for(int eventID : eventIDList){
				Object[] eventData = AmberServer.getDatabase().getEventData(eventID);
				String eventType = (String)eventData[1];
				String eventTime = (String)eventData[2];
				System.out.println(eventData[3]);
				double eventLat = (double)eventData[3];
				double eventLon = (double)eventData[4];
				//				byte[] eventImage = (byte[]) eventData[5];	// EventImage
				Event event = new Event(eventType, eventTime, eventLat, eventLon, null, vehicleName);
				// Add events to the current Vehicle
				vehicle.getEventList().add(event);
			}
			// Add vehicles to the current User
			vehicle.setVehicleID(vehicle.getVehicleID());
			vehicle.setVehicleName(vehicleName);
			this.vehicleList.add(vehicle);
		}
		this.userID = adminID;
		return this;
	}
	/**
	 * 
	 * @return
	 */
	public ArrayList<Vehicle> getVehicles(){
		return this.vehicleList;
	}
	/**
	 * 
	 * @return
	 */
	public int getUserID() {
		return userID;
	}

}
