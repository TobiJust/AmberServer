package de.thwildau.model;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public class Event implements Serializable {
	private static final long serialVersionUID = 8679932140958446049L;

	private String eventType;
	private String timeStamp;
	private String vehicleID;
	private double lat;
	private double lon;
	private byte[] eventImage;

	public Event() {
		// TODO Auto-generated constructor stub
	}
	
	public Event(String eventType, String eventTime, double eventLat,
			double eventLon, byte[] eventImage) {
		this.eventType = eventType;
		this.timeStamp = eventTime;
		this.lat = eventLat;
		this.lon = eventLon;
		this.eventImage = eventImage;
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


}