package de.thwildau.stream;

import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

import de.thwildau.feature.Screenshot;
import de.thwildau.model.Telemetry;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;
import de.thwildau.webserver.Websocket;
import de.thwildau.webserver.WebsocketResponse;

/**
 * 
 * @author Tobias Just
 *
 */
public class VideoStreamer extends Thread {

	private ConcurrentHashMap<Integer, VideoStream> streams = new ConcurrentHashMap<Integer, VideoStream>();
	private byte[] image;
	private boolean watch = false;
	private Session session;
	private VideoStream videoStream;
	private WebsocketResponse response;

	/**
	 * 
	 * @return The current image as byte array.
	 */
	public byte[] getValue(){
		return this.image;
	}

	/**
	 * 
	 * @param session
	 */
	public void startStream(Session session){
		this.session = session;
		if(!this.watch)
			this.watch = true;
		//		start();
		ServerLogger.log("Stream started" , Constants.DEBUG);
	}
	/**
	 * 
	 */
	public void stopStream() {
		if(this.watch)
			this.watch = false;
		ServerLogger.log("Stream stopped" , Constants.DEBUG);
	}

	/**
	 * Start recording the stream with given vehicle id.
	 * @param vehicleID		ID of the vehicle to stop the video.
	 */
	public void startRecord(int userID, int vehicleID) {
		this.videoStream = new VideoStream(this, userID, vehicleID);
		this.videoStream.startRecord();
		streams.put(vehicleID, this.videoStream);
	}
	/**
	 * Stop recording the stream with given vehicle id.
	 * @param vehicleID		ID of the vehicle to stop the video.
	 */
	public void stopRecord(int vehicleID){
		if(streams.get(vehicleID) != null)
			streams.get(vehicleID).getVideoStream();
		ServerLogger.log("Stop Recording for Vehicle " + vehicleID, Constants.DEBUG);
	}
	/**
	 * 
	 * @param vehicleID
	 */
	public void screenshot() {
		// Set the current stream image as new screenshot image 
		Screenshot.websocketResponse = this.response;
	}

	/**
	 * 
	 * @param response
	 */
	public void setResponseData(WebsocketResponse response) {	
		try {
			if(response != null && this.session != null){
				this.response = response;
				if(watch){
					for(Session s : Websocket.sessions.keySet())
						s.getAsyncRemote().sendText(response.toJSON());
					//						s.getBasicRemote().sendText(response.toJSON());
				}	
			}
			if(response != null && Constants.LOG)
				logData((Telemetry)response.getData());
		} catch (Exception e) {
		}
	}
	/**
	 * 
	 * @return
	 */
	public WebsocketResponse getResponse() {
		return this.response;
	}

	/**
	 * 
	 */
	@Override
	public void run() {
		while(watch){
			try {
				if(getResponse() != null)
					this.session.getBasicRemote().sendText(getResponse().toJSON());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void logData(Telemetry telemetry) {
		StreamManager.log(telemetry.toString());
	}


}
