package de.thwildau.model;

import java.util.HashMap;
import java.util.List;

import de.thwildau.util.Util;

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

	private static final byte POS_N 		= 0x00;
	private static final byte POS_E 		= 0x01;
	private static final byte POS_H			= 0x02;
	private static final byte ACC_X 		= 0x03;
	private static final byte ACC_Y 		= 0x04;
	private static final byte ACC_Z 		= 0x05;
	private static final byte GYRO_X 		= 0x06;
	private static final byte GYRO_Y 		= 0x07;
	private static final byte GYRO_Z 		= 0x08;
	private static final byte SPEED  		= 0x09;
	private static final byte RPM   		= 0x0A;
	private static final byte ENG_LOAD 		= 0x0B;
	private static final byte COOL_TEMP 	= 0x0C;
	private static final byte AIR_FLOW 		= 0x0D;
	private static final byte INLET_PRESS 	= 0x0E;
	private static final byte INLET_TEMP 	= 0x0F;
	private static final byte FUEL_LVL 		= 0x10;
	private static final byte FUEL_PRESS 	= 0x11;
	private static final byte ENG_KM 		= 0x12;

	// ...

	private String fuel 			= "30";
	private String speed 			= "0";
	private String revolutions 		= "0";
	private String gyro_x 			= "0";
	private String gyro_y 			= "0";
	private String gyro_z 			= "0";
	private double lat 				= 52.13;
	private double lon 				= 13.37;
	private String drive 			= "";
	private String airFlow 			= "";
	private String airPressure 		= "";
	private String airTemperature 	= "";
	private String coolingLiqTemp 	= "";
	private String fuelPressure 	= "";
	private String enviroPressure 	= "";
	private String kmAtAll 			= "";
	private String km 				= "0";
	private String engineLoad 		= "";
	private String acceleration_x	= "";
	private String acceleration_y	= "";
	private String acceleration_z	= "";

	public Telemetry(){

	}

	public void addData(HashMap<Byte, List<Byte>> data) {
		for(byte b : data.keySet()){
			switch(b){
			case SPEED:
				this.speed = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case FUEL_LVL:
				this.fuel = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case RPM:
				this.revolutions = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case FUEL_PRESS:
				this.fuelPressure = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case POS_N:
				if(parseCoordinates(Util.bytesToStringUTFCustom(data.get(b))) > 0.0)
					this.lat = parseCoordinates(Util.bytesToStringUTFCustom(data.get(b)));
				System.out.println("THIS:LAT " + this.lat);
				break;
			case POS_E:
				if(parseCoordinates(Util.bytesToStringUTFCustom(data.get(b))) > 0.0)
					this.lon = parseCoordinates(Util.bytesToStringUTFCustom(data.get(b)));
				System.out.println("THIS:LON " + this.lon);
				break;
			case ENG_KM:
				this.km = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case GYRO_X:
				this.gyro_x = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case GYRO_Y:
				this.gyro_y = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case GYRO_Z:
				this.gyro_z = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case ENG_LOAD:
				this.engineLoad = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case AIR_FLOW:
				this.airFlow = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case COOL_TEMP:
				this.coolingLiqTemp = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case INLET_PRESS:
				this.airPressure = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case ACC_X:
				this.acceleration_x = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case ACC_Y:
				this.acceleration_y = Util.bytesToStringUTFCustom(data.get(b));
				break;
			case ACC_Z:
				this.acceleration_z = Util.bytesToStringUTFCustom(data.get(b));
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

	public String getEngineLoad() {
		return engineLoad;
	}

	public void setEngineLoad(String engineLoad) {
		this.engineLoad = engineLoad;
	}

	public String getGyro_x() {
		return gyro_x;
	}

	public void setGyro_x(String gyro_x) {
		this.gyro_x = gyro_x;
	}

	public String getGyro_y() {
		return gyro_y;
	}

	public void setGyro_y(String gyro_y) {
		this.gyro_y = gyro_y;
	}

	public String getGyro_z() {
		return gyro_z;
	}

	public void setGyro_z(String gyro_z) {
		this.gyro_z = gyro_z;
	}

	public String getAcceleration_x() {
		return acceleration_x;
	}

	public void setAcceleration_x(String acceleration_x) {
		this.acceleration_x = acceleration_x;
	}

	public String getAcceleration_y() {
		return acceleration_y;
	}

	public void setAcceleration_y(String acceleration_y) {
		this.acceleration_y = acceleration_y;
	}

	public String getAcceleration_z() {
		return acceleration_z;
	}

	public void setAcceleration_z(String acceleration_z) {
		this.acceleration_z = acceleration_z;
	}

	private double parseCoordinates(String coordsInMinutesSeconds){
		System.out.println("COORDS " + coordsInMinutesSeconds);
		if(coordsInMinutesSeconds.contains("--"))
			return 0.0;
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

}
