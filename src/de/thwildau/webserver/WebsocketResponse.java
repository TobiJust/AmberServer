package de.thwildau.webserver;


public class WebsocketResponse{
	
	private String id;
	private Object data;

	public static final String LOGIN = "loginACK";
	public static final String LOGOUT = "logoutACK";
	public static final String COMMAND = "commandACK";
	public static final String CARS_RESPONSE = "carsACK";
	public static final String START_RECORD = "startRecordACK";
	public static final String STOP_RECORD = "stopRecordACK";
	public static final String ERROR = "error";
	public static final String STREAM_CLOSED = "streamClosed";
//	{
//		"loginACK", logoutACK, carsACK, commandACK, startRecordACK, stopRecordACK, error
//	}
	
	public WebsocketResponse(String id, Object response){
		this.id = id;
		this.data = response;
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



}
