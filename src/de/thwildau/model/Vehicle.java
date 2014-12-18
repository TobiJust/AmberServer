package de.thwildau.model;

import java.io.Serializable;
import java.util.ArrayList;

import de.thwildau.server.AmberServer;

public class Vehicle implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -1208507633758359464L;
	private ArrayList<Event> eventList;
	private String vehicleName;
	public String date;
	public int image;
	public boolean boxed;


	public Vehicle(){
		eventList = new ArrayList<Event>();
	}

	public Vehicle(String name){
		this.vehicleName = name;
		eventList = new ArrayList<Event>();
	}

	public Vehicle(String name, String date, int image, boolean boxed) {
		this.vehicleName = name;
		this.date = date;
		this.image = image;
		this.boxed = boxed;
	}


	public ArrayList<Event> getEventList(){
		return this.eventList;
	}

	public String getVehicleName(){
		return this.vehicleName;
	}

	public Vehicle prepareVehicle(String vehicleID) {
		Object[] vehicleData = AmberServer.getDatabase().getEvents(vehicleID);
		ArrayList<Integer> eventIDList = (ArrayList<Integer>) vehicleData[1];
		this.vehicleName = (String)vehicleData[0];
		for(int eventID : eventIDList){
			Object[] eventData = AmberServer.getDatabase().getEventData(eventID);
			String eventType = (String)eventData[1];
			String eventTime = (String)eventData[2];
			double eventLat = (double)eventData[3];
			double eventLon = (double)eventData[4];
			byte[] eventImage = (byte[]) eventData[5];	// EventImage

			Event event = new Event(eventType, eventTime, eventLat, eventLon, eventImage);
			// Add events to the current Vehicle
			this.getEventList().add(event);

		}
		return this;
	}

}
