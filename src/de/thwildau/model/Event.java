package de.thwildau.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.thwildau.util.Constants;
import de.thwildau.util.Util;

/**
 * 
 * @author Tobias Just
 *
 */
public class Event implements Serializable {

	private static final long serialVersionUID = 8679932140958446049L;

	/**
	 * Event Data Identifier
	 */
	private static final byte LAT 			= 0x00;
	private static final byte LON 			= 0x01;
	private static final byte TIME 			= 0x02;	
	/**
	 * Event Type Identifier
	 */
	private static final byte ACC 			= 0x00;
	private static final byte GYRO 			= 0x01;

	private static final byte SPEED 		= 0x03;
	private static final byte RPM 			= 0x04;
	private static final byte COOL_TEMP 	= 0x05;
	private static final byte FUEL 			= 0x06;

	private int id;
	private String eventType;
	private String timeStamp = "11012015-133745";
	private int eventIdentifier;
	private int vehicleID;
	private String vehicleName;
	private double lat = 52.13;
	private double lon = 13.37;
	private byte[] eventImage;
	private String eventMessage;


	public Event() {

	}
	/**
	 * The Event which will be initialized 
	 * @param eventType
	 * @param eventTime
	 * @param eventLat
	 * @param eventLon
	 * @param eventImage
	 * @param vehicleName
	 */
	public Event(String eventType, String eventTime, double eventLat,
			double eventLon, byte[] eventImage, String vehicleName) {
		this.eventType = eventType;
		this.timeStamp = eventTime;
		this.lat = eventLat;
		this.lon = eventLon;
		this.eventImage = eventImage;
		this.vehicleName = vehicleName;
		getMessageByType();
	}

	public Event(int eventID, String eventType, String eventTime) {
		this.id = eventID;
		this.eventType = eventType;
		this.timeStamp = eventTime;
		getMessageByType();
	}	

	public String getEventTypeByID(int type){
		switch(type){
		case ACC:
			this.eventType = "Accelerometer";
			break;
		case SPEED:
			this.eventType = "Speed";
			break;
		case FUEL:
			this.eventType = "Fuel";
			break;
		case COOL_TEMP:
			this.eventType = "Coolant";
			break;
		case GYRO:
			this.eventType = "Gyro";
			break;
		default:
			break;
		}
		return this.eventType;
	}

	public void addData(HashMap<Byte, List<Byte>> data) {
		System.out.println("EVENT DATA " + data);
		for(byte b : data.keySet()){
			switch(b){
			case LAT:
				if(parseCoordinates(Util.bytesToStringUTFCustom(data.get(b))) > 0.0)
					this.lat = parseCoordinates(Util.bytesToStringUTFCustom(data.get(b)));
				System.out.println("LAT " + this.lat);
				break;
			case LON:
				if(parseCoordinates(Util.bytesToStringUTFCustom(data.get(b))) > 0.0)
					this.lon = parseCoordinates(Util.bytesToStringUTFCustom(data.get(b)));
				System.out.println("LON " + this.lon);
				break;
			case TIME:
				this.timeStamp = Util.bytesToStringUTFCustom(data.get(b));
				break;
			default:
				break;
			}
		}
	}
	private void getMessageByType(){
		switch(this.eventType){
		case "Accelerometer":
			this.eventMessage = Constants.ACC_EVENT;
			break;
		case "Speed":
			this.eventMessage = Constants.SPEED_EVENT;
			break;
		case "Fuel":
			this.eventMessage = Constants.FUEL_EVENT;
			break;
		case "Turn":
			this.eventMessage = Constants.TURN_EVENT;
			break;
		case "Coolant":
			this.eventMessage = Constants.COOLANT_EVENT;
			break;
		case "Gyro":
			this.eventMessage = Constants.GYRO_EVENT;
			break;
		default:
			this.eventMessage = Constants.DEFAULT_EVENT;
			break;
		}
	}

	private double parseCoordinates(String coordsInMinutesSeconds){
		System.out.println("COORDS IN MINUTES " + coordsInMinutesSeconds);
		if(coordsInMinutesSeconds.contains("--"))
			return 0.1;
		int index = 0;
		String[] split = coordsInMinutesSeconds.split("");
		if(split[0].equals("0"))
			index = 1;		
		double d = Double.parseDouble(split[index]+split[index+1]);
		double m = Double.parseDouble(split[index+2]+split[index+3]);
		double s = Double.parseDouble(split[index+5]+split[index+6]+"."+split[index+7]);
		char sign = coordsInMinutesSeconds.charAt(index+8);	

		return Math.signum(d) * (Math.abs(d) + (m / 60.0) + s);
//		return Math.signum(d) * (Math.abs(d) + (m / 60.0) + (s / 3600.0));
	}

	public String getVehicleName() {
		return vehicleName;
	}

	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}

	public void setVehicleID(int id){
		this.vehicleID = id;
	}

	public void setLatitude(double lat){
		this.lat = lat;
	}

	public void setLongitude(double lon){
		this.lon = lon;
	}

	public int getVehicleID(){
		return this.vehicleID;
	}

	public double getLatitude(){
		return this.lat;
	}

	public double getLongitude(){
		return this.lon;
	}

	public void setEventType(int eventType) {
		this.eventType = getEventTypeByID(eventType);
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
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
	public void setEventImage(byte[] eventImage) {
		this.eventImage = eventImage;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getEventMessage() {
		return eventMessage;
	}

	public void setEventMessage(String eventMessage) {
		this.eventMessage = eventMessage;
	}

	@Override
	public String toString() {
		return "Event [eventType=" + eventType + ", timeStamp=" + timeStamp
				+ ", vehicleID=" + vehicleID + ", lat=" + lat + ", lon=" + lon
				+ ", eventImage=" + Arrays.toString(eventImage) + "]";
	}

}