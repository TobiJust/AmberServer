package de.thwildau.stream;

import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;
import de.thwildau.webserver.WebsocketResponse;

/**
 * 
 * @author Tobias Just
 *
 */
public class VideoStreamer extends Thread {

	private ConcurrentHashMap<Integer, VideoStream> streams = new ConcurrentHashMap<Integer, VideoStream>();
	private byte[] image;
	private boolean running = false;
	private boolean watch = true;
	private boolean recording = false;
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
		System.out.println(StreamManager.videoStreams.keySet());
		this.session = session;
		if(!this.watch)
			this.watch = true;
		ServerLogger.log("Stream started" , Constants.DEBUG);
	}
	/**
	 * 
	 */
	public void stopStream() {
		if(this.running)
			this.running = false;
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
		if(!this.recording)
			this.recording = true;
	}
	/**
	 * Stop recording the stream with given vehicle id.
	 * @param vehicleID		ID of the vehicle to stop the video.
	 */
	public void stopRecord(int vehicleID){
		if(this.recording)
			this.recording = false;
		if(streams.get(vehicleID) != null)
			streams.get(vehicleID).getVideoStream();
		ServerLogger.log("Stop Recording for Vehicle " + vehicleID, Constants.DEBUG);
	}

	public void setImage(WebsocketResponse response) {
		try {
			if(response != null && this.session != null){
				System.out.println(watch + " " +response);
				this.response = response;
				if(watch){
					this.session.getBasicRemote().sendText(response.toJSON());
				}	
			}
			Thread.sleep(500);
		} catch (Exception e) {
		}
	}

	public WebsocketResponse getResponse() {
		return this.response;
	}


}
