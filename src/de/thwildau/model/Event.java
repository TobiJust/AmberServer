package de.thwildau.model;

import java.io.Serializable;
import java.util.Date;

public class Event implements Serializable {
	private static final long serialVersionUID = 8679932140958446049L;

	public static enum Type {
		TURN, ACCIDENT, ERROR
	}

	private Date timeStamp;
	private String vehicleID;
	private double lat;
	private double lon;

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

	public Date getTimeStamp(){
		return this.timeStamp;
	}


}