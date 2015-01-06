package de.thwildau.model;

import java.util.HashMap;
import java.util.List;

public class Telemetry {

	private final int COORD_X = 0;
	private final int COORD_Y = 1;
	private final int SPEED = 2;
	private final int FUEL = 3;
	private final int REVOLUTIONS = 4;
	private final int PRESSURE = 5;
	// ...

	private String fuel;
	private String speed;
	private String revolutions;
	private String lat;
	private String lon;
	private String drive;
	private String airFlow;
	private String airPressure;
	private String airTemperature;
	private String coolingLiqTemp;
	private String fuelPressure;
	private String enviroPressure;
	private String kmAtAll;
	private String km;
	
	public Telemetry(){

	}

	public void addData(HashMap<Byte, List<Byte>> data) {
		for(byte b : data.keySet()){
			switch(b & 0xFF){
			case SPEED:
				this.speed = bytesToStringUTFCustom(data.get(b));
				break;
			case FUEL:
				this.fuel = bytesToStringUTFCustom(data.get(b));
				break;
			case REVOLUTIONS:
				this.revolutions = bytesToStringUTFCustom(data.get(b));
				break;
			case PRESSURE:
				this.airPressure = bytesToStringUTFCustom(data.get(b));
				break;
			case COORD_X:
				this.lat = bytesToStringUTFCustom(data.get(b));
				break;
			case COORD_Y:
				this.lon = bytesToStringUTFCustom(data.get(b));
				break;
			default:
				break;
			}
		}
	}
	public String getFuel() {
		return fuel;
	}

	public void setFuel(String fuel) {
		this.fuel = fuel;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getRevolutions() {
		return revolutions;
	}

	public void setRevolutions(String revolutions) {
		this.revolutions = revolutions;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getDrive() {
		return drive;
	}

	public void setDrive(String drive) {
		this.drive = drive;
	}

	public String getAirFlow() {
		return airFlow;
	}

	public void setAirFlow(String airFlow) {
		this.airFlow = airFlow;
	}

	public String getAirPressure() {
		return airPressure;
	}

	public void setAirPressure(String airPressure) {
		this.airPressure = airPressure;
	}

	public String getAirTemperature() {
		return airTemperature;
	}

	public void setAirTemperature(String airTemperature) {
		this.airTemperature = airTemperature;
	}

	public String getCoolingLiqTemp() {
		return coolingLiqTemp;
	}

	public void setCoolingLiqTemp(String coolingLiqTemp) {
		this.coolingLiqTemp = coolingLiqTemp;
	}

	public String getFuelPressure() {
		return fuelPressure;
	}

	public void setFuelPressure(String fuelPressure) {
		this.fuelPressure = fuelPressure;
	}

	public String getEnviroPressure() {
		return enviroPressure;
	}

	public void setEnviroPressure(String enviroPressure) {
		this.enviroPressure = enviroPressure;
	}

	public String getKmAtAll() {
		return kmAtAll;
	}

	public void setKmAtAll(String kmAtAll) {
		this.kmAtAll = kmAtAll;
	}

	public String getKm() {
		return km;
	}

	public void setKm(String km) {
		this.km = km;
	}

	public static String bytesToStringUTFCustom(List<Byte> bytes) {
		char[] buffer = new char[bytes.size()];
		for(int i = 0; i < buffer.length; i++) {
			int bpos = i;
			char c = (char)(((bytes.get(bpos)&0xFF)));
			buffer[i] = c;
		}
		return new String(buffer);
	}

	@Override
	public String toString() {
		return "Telemetry [fuel=" + fuel + ", speed=" + speed
				+ ", revolutions=" + revolutions + ", lat=" + lat + ", lon="
				+ lon + ", drive=" + drive + ", airFlow=" + airFlow
				+ ", airPressure=" + airPressure + ", airTemperature="
				+ airTemperature + ", coolingLiqTemp=" + coolingLiqTemp
				+ ", fuelPressure=" + fuelPressure + ", enviroPressure="
				+ enviroPressure + ", kmAtAll=" + kmAtAll + ", km=" + km + "]";
	}

}
