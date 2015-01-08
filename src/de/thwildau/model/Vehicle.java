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
	private int vehicleID;
	public String date;
	public byte[] image;
	public boolean boxed;


	public Vehicle(){
		eventList = new ArrayList<Event>();
	}

	public Vehicle(String name){
		this.vehicleName = name;
		eventList = new ArrayList<Event>();
	}

	public Vehicle(String name, String date, byte[] image, boolean boxed) {
		this.vehicleName = name;
		this.date = date;
		this.image = image;
		this.boxed = boxed;
	}


	public ArrayList<Event> getEventList(){
		return this.eventList;
	}
	public void setVehicleID(int vehicleID) {
		this.vehicleID = vehicleID;
	}

	public String getVehicleName(){
		return this.vehicleName;
	}

	public static ArrayList<Event> prepareEventList(int vehicleID){
		ArrayList<Event> events = new ArrayList<Event>();
		Object[] vehicleData = AmberServer.getDatabase().getEvents(vehicleID);
		ArrayList<Integer> eventIDList = (ArrayList<Integer>) vehicleData[1];
		System.out.println("VEHICLE - IDLIST " + eventIDList );
		for(int eventID : eventIDList){
			Object[] eventData = AmberServer.getDatabase().getEventData(eventID);
			String eventType = (String)eventData[1];
			String eventTime = (String)eventData[2];
//			double eventLat = (double)eventData[3];
//			double eventLon = (double)eventData[4];
//			byte[] eventImage = (byte[]) eventData[5];	// EventImage

			Event event = new Event(eventID, eventType, eventTime);
			event.setVehicleID(vehicleID);
			// Add events to the current Vehicle
			events.add(event);
		}
		return events;
	}
	
	public Vehicle prepareVehicle(int vehicleID) {
		Object[] vehicleData = AmberServer.getDatabase().getEvents(vehicleID);
		this.vehicleName = (String)vehicleData[0];
		ArrayList<Integer> eventIDList = (ArrayList<Integer>) vehicleData[1];
		this.setImage((byte[]) vehicleData[2]);
		for(int eventID : eventIDList){
			Object[] eventData = AmberServer.getDatabase().getEventData(eventID);
			String eventType = (String)eventData[1];
			String eventTime = (String)eventData[2];
			double eventLat = (double)eventData[3];
			double eventLon = (double)eventData[4];
			byte[] eventImage = (byte[]) eventData[5];	// EventImage

			Event event = new Event(eventType, eventTime, eventLat, eventLon, eventImage, this.vehicleName);
			// Add events to the current Vehicle
			this.getEventList().add(event);
		}
		this.vehicleID = vehicleID;
		return this;
	}

	public int getVehicleID() {
		return vehicleID;
	}

	public String getDate() {
		return date;
	}

	public byte[] getImage() {
		return image;
	}

	public boolean isBoxed() {
		return boxed;
	}

	public void setAlarmStatus(int status) {		
		this.boxed = (status == 1) ? true : false;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setVehicleName(String name) {
		this.vehicleName = name;		
	}

	public void setImage(byte[] data) {
		this.image = data;
	}


}
