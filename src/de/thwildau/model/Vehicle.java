package de.thwildau.model;

import java.io.Serializable;
import java.util.ArrayList;

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

}
