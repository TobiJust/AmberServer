package de.thwildau.webserver;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
/**
 * 
 * @author Tobias Just
 *
 */
public class WebsocketResponse{

	private String id;
	private Object data;
	private Object notification;
	private byte[] image;

	public static final String LOGIN 			= "loginACK";
	public static final String LOGOUT 			= "logoutACK";
	public static final String COMMAND 			= "commandACK";
	public static final String CARS_RESPONSE 	= "carsACK";
	public static final String START_RECORD 	= "startRecordACK";
	public static final String STOP_RECORD 		= "stopRecordACK";
	public static final String ERROR 			= "error";
	public static final String STREAM_STARTED 	= "streamStarted";
	public static final String STREAM_CLOSED 	= "streamClosed";
	public static final String TELEMETRY 		= "telemetry";
	public static final String SCREENSHOT 		= "screenshotACK";
	public static final String REGISTER_VEHICLE	= "addCarACK";

	public WebsocketResponse(String id, Object response){
		this.id = id;
		this.data = response;
	}
	public WebsocketResponse(String id, byte[] image, Object response, Object notification){
		this.id = id;
		this.data = response;
		this.image = image;
		this.notification = notification;
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

	public Object getNotification() {
		return notification;
	}
	public void setNotification(Object notification) {
		this.notification = notification;
	}
	/**
	 * JSONify this response object to send it as json string to the 
	 * web app user. This enable a key value set for simple decoding 
	 * on the web app.
	 * 
	 * @return	Websocket response object as JSON String.
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
