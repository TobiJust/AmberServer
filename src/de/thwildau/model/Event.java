package de.thwildau.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 
 * @author Tobi Just
 *
 */
public class Event implements Serializable {
	
	private static final long serialVersionUID = 8679932140958446049L;

	private int id;
	private String eventType;
	private String timeStamp;
	private String vehicleID;
	private String vehicleName;
	private double lat;
	private double lon;
	private byte[] eventImage;
	

	public Event(String eventType, String eventTime, double eventLat,
			double eventLon, byte[] eventImage, String vehicleName) {
		this.eventType = eventType;
		this.timeStamp = eventTime;
		this.lat = eventLat;
		this.lon = eventLon;
		this.eventImage = eventImage;
		this.vehicleName = vehicleName;
	}

	public Event(int eventID, String eventType, String eventTime) {
		this.id = eventID;
		this.eventType = eventType;
		this.timeStamp = eventTime;
	}

	public String getVehicleName() {
		return vehicleName;
	}

	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}

	public void setVehicleID(String id){
		this.vehicleID = id;
	}

	public void setLatitude(double lat){
		this.lat = lat;
	}

	public void setLongitude(double lon){
		this.lon = lon;
	}

	public String getVehicleID(){
		return this.vehicleID;
	}

	public double getLatitude(){
		return this.lat;
	}

	public double getLongitude(){
		return this.lon;
	}

	public String getTimeStamp(){
		return this.timeStamp;
	}
	public String getEventType(){
		return this.eventType;
	}
	public byte[] getEventImage() {
		return eventImage;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	
	@Override
	public String toString() {
		return "Event [eventType=" + eventType + ", timeStamp=" + timeStamp
				+ ", vehicleID=" + vehicleID + ", lat=" + lat + ", lon=" + lon
				+ ", eventImage=" + Arrays.toString(eventImage) + "]";
	}
}