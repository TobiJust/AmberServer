package de.thwildau.model;

import java.awt.Image;
import java.awt.Toolkit;
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
	private Image eventImage;

	public Event(){
		eventImage = Toolkit.getDefaultToolkit().getImage("/img/berlin.jpg");
	}

	public Image getEventImage() {
		return eventImage;
	}


}