package de.thwildau.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;



public class VideoStreamer extends Thread {

	private static final Logger LOG = LoggerFactory.getLogger(VideoStreamer.class);
	private ConcurrentHashMap<String, VideoStream> streams = new ConcurrentHashMap<String, VideoStream>();
	int i = 0;
	private byte[] image;
	private boolean running = false;
	private boolean watch = true;
	private boolean recording = true;
	private Session session;
	private VideoStream videoStream;


	@Override
	public void run() {		
		while(running){
			//			try {
			//				System.out.println(this.image + " " + this.session);
			//				if(this.image != null && this.session != null){
			////					if(watch){
			////						ByteBuffer buf = ByteBuffer.wrap(this.image);
			////						this.session.getBasicRemote().sendBinary(buf);
			////						Thread.sleep(50);
			////					}
			//					if(recording){
			//						this.videoStream.writeToStream(this.image);
			//					}
			//				}
			//			} catch (IOException e) {
			//				e.printStackTrace();
			//			} catch (InterruptedException e) {
			//				e.printStackTrace();
			//			}
		}		
	}

	public byte[] getValue(){
		return this.image;
	}
	public void setImage(byte[] data){		
		//		this.image = data;
		try {
			System.out.println(data + " " + (this.session != null) + " " + watch + " " + recording);
			if(data != null && this.session == null){
				if(watch){
					ByteBuffer buf = ByteBuffer.wrap(data);
					this.session.getBasicRemote().sendBinary(buf);
				}
				if(recording){
					this.videoStream.writeToStream(data);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startStream() {
		if(!this.running){
			this.running = true;
			start();
		}
		ServerLogger.log("Stream started" , Constants.DEBUG);
	}

	public void startStream(Session session){
		this.session = session;
		if(!this.running){
			this.running = true;
			start();
		}
		if(!this.watch)
			this.watch = true;
		ServerLogger.log("Stream started" , Constants.DEBUG);
	}

	public void stopStream() {
		if(this.running)
			this.running = false;
		ServerLogger.log("Stream stopped" , Constants.DEBUG);
	}

	public void startRecord(String vehicleID) {
		this.videoStream = new VideoStream(vehicleID);
		streams.put(vehicleID, this.videoStream);
		if(!this.recording)
			this.recording = true;
	}
	public void stopRecord(String vehicleID){
		streams.get(vehicleID).getVideoStream();
		if(this.recording)
			this.recording = false;
	}

	public void setImage(String response) {
		try {
			System.out.println(response);
			if(response != null && this.session != null){
				if(watch){
					this.session.getBasicRemote().sendText(response);
				}
			
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
