package de.thwildau.model;

public class Telemetry {

	private final int SPEED = 0;
	private final int FUEL = 1;
	private final int REVOLUTIONS = 2;
	private final int PRESSURE = 3;

	private String fuel;
	private String speed;
	private String revolutions;
	// ...

	public Telemetry(){

	}

	public void addData(byte id, String data) {
		switch(id){
		case SPEED:
			this.speed = data;
			break;
		case FUEL:
			this.fuel = data;
			break;
		case REVOLUTIONS:
			this.revolutions = data;
			break;
		case PRESSURE:
			break;
		default:
			break;
		}
	}

}
