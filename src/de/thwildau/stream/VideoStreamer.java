package de.thwildau.stream;

import java.io.BufferedOutputStream;
import java.nio.ByteBuffer;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class VideoStreamer implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(VideoStreamer.class);
	int i = 0;
	private byte[] image;
	private boolean run = false;
	BufferedOutputStream bos = null;
	
	
	@Override
	public void run() {
		
		while(true){
			try {
				run = true;
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	public byte[] getValue(){
		return this.image;
	}
	public void setImage(byte[] data){
		this.image = data;
	}

	public boolean stream(Session session) {
		while(run){
			ByteBuffer buf = ByteBuffer.wrap(this.image);
			session.getAsyncRemote().sendBinary(buf); 

		}
		return true;
		
	}
	
}
