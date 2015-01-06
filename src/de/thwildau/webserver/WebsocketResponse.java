package de.thwildau.webserver;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;


public class WebsocketResponse{

	private String id;
	private Object data;
	private byte[] image;

	public static final String LOGIN = "loginACK";
	public static final String LOGOUT = "logoutACK";
	public static final String COMMAND = "commandACK";
	public static final String CARS_RESPONSE = "carsACK";
	public static final String START_RECORD = "startRecordACK";
	public static final String STOP_RECORD = "stopRecordACK";
	public static final String ERROR = "error";
	public static final String STREAM_STARTED = "streamStarted";
	public static final String STREAM_CLOSED = "streamClosed";
	public static final String TELEMETRY = "telemetry";

	public WebsocketResponse(String id, Object response){
		this.id = id;
		this.data = response;
	}
	public WebsocketResponse(String id, byte[] image, Object response){
		this.id = id;
		this.data = response;
		this.image = image;
	}

	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * 
	 * @param response
	 * @return
	 */
	public String toJSON(){
		ObjectMapper mapper = new ObjectMapper();

		String json = null;
		try {
			json = mapper.writeValueAsString(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;			
	}

}
