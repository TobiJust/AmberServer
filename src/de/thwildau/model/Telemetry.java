package de.thwildau.model;

import java.util.HashMap;
import java.util.List;

public class Telemetry {

	/*
	#define FIELD_TYPE_POS_N    0x00
	#define FIELD_TYPE_POS_E    0x01
	#define FIELD_TYPE_POS_H    0x02
	#define FIELD_TYPE_ACC_X    0x03
	#define FIELD_TYPE_ACC_Y    0x04
	#define FIELD_TYPE_ACC_Z    0x05
	#define FIELD_TYPE_GYRO_X   0x06
	#define FIELD_TYPE_GYRO_Y   0x07
	#define FIELD_TYPE_GYRO_Z   0x08

	#define FIELD_TYPE_OBD_SPEED        0x09
	#define FIELD_TYPE_OBD_RPM          0x0A
	#define FIELD_TYPE_OBD_ENG_LOAD     0x0B
	#define FIELD_TYPE_OBD_COOL_TEMP    0x0C
	#define FIELD_TYPE_OBD_AIR_FLOW     0x0D
	#define FIELD_TYPE_OBD_INLET_PRESS  0x0E
	#define FIELD_TYPE_OBD_INLET_TEMP   0x0F
	#define FIELD_TYPE_OBD_FUEL_LVL     0x10
	#define FIELD_TYPE_OBD_FUEL_PRESS   0x11
	#define FIELD_TYPE_OBD_ENG_KM       0x12
	*/
	
	private final byte POS_N = 0x00;
	private final byte POS_E = 0x01;
	private final byte POS_H = 0x02;
	private final byte ACC_X = 0x03;
	private final byte ACC_Y = 0x04;
	private final byte ACC_Z = 0x05;
	private final byte GYRO_X = 0x06;
	private final byte GYRO_Y = 0x07;
	private final byte GYRO_Z = 0x08;
	private final byte SPEED  = 0x09;
	private final byte RPM   = 0x0A;
	private final byte FUEL_LVL = 0x10;
	private final byte FUEL_PRESS = 0x11;
	
	// ...

	private String fuel;
	private String speed;
	private String revolutions;
	private double lat;
	private double lon;
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
			switch(b){
			case SPEED:
				this.speed = bytesToStringUTFCustom(data.get(b));
				break;
			case FUEL_LVL:
				this.fuel = bytesToStringUTFCustom(data.get(b));
				break;
			case RPM:
				this.revolutions = bytesToStringUTFCustom(data.get(b));
				break;
			case FUEL_PRESS:
				this.airPressure = bytesToStringUTFCustom(data.get(b));
				break;
			case POS_N:
				this.lat = parseCoordinates(bytesToStringUTFCustom(data.get(b)));
				break;
			case POS_E:
				this.lon = parseCoordinates(bytesToStringUTFCustom(data.get(b)));
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

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
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

	private double parseCoordinates(String coordsInMinutesSeconds){
		int index = 0;
		String[] split = coordsInMinutesSeconds.split("");
		if(split[0].equals("0"))
			index = 1;		
		double d = Double.parseDouble(split[index]+split[index+1]);
		double m = Double.parseDouble(split[index+2]+split[index+3]);
		double s = Double.parseDouble(split[index+5]+split[index+6]+"."+split[index+7]);
		char sign = coordsInMinutesSeconds.charAt(index+8);	

		return Math.signum(d) * (Math.abs(d) + (m / 60.0) + (s / 3600.0));
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
