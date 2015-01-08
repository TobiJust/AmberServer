package de.thwildau.obu;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;

import de.thwildau.model.ImageData;
import de.thwildau.model.Telemetry;
import de.thwildau.obu.model.FrameObject;
import de.thwildau.obu.model.TelemetryFrameObject;
import de.thwildau.stream.StreamManager;
import de.thwildau.stream.VideoStream;
import de.thwildau.stream.VideoStreamer;
import de.thwildau.webserver.WebsocketResponse;

public class OBUResponseHandler {


	private LinkedList<FrameObject> frameList = new LinkedList<FrameObject>();
	public static ConcurrentHashMap<Integer, IoSession> handlers = new ConcurrentHashMap<Integer, IoSession>();


	int index = 0;
	int length = 0;
	private VideoStream videoStream;
	private ByteArrayOutputStream inBuffer;
	private int frame_count;
	private int frame_offset;
	int n = 0;

	ImageData imageData;
	Telemetry telemetry = new Telemetry();

	private int ind = 0;
	private boolean isCorrect;

	public OBUResponseHandler(){
	}

	public byte[] getBuffer(){
		return this.inBuffer.toByteArray();
	}
	/**
	 * Add data till the Frame is complete. Otherwise create a new Frame.
	 * 
	 * @param data Data received from OBU
	 * @return true, if Frame is complete
	 * 		   false, if Frame needs more Data
	 * @throws Exception 
	 */
	public boolean addData(byte[] data) throws Exception{	

		if(frameList.size() < 1 || frameList.get(frameList.size()-1).checkLength()){ 
			System.out.println("LAST FRAME");
			if(frameList.size() > 1){
				for(byte b : frameList.get(frameList.size()-1).getFrame())
					System.out.print(b + " ");
				System.out.println();
				System.out.println("CHECKSUM " + frameList.get(frameList.size()-1).checksum());
				System.out.println("-----------------------------------------------------------------------------");
				Thread.sleep(10000);
			}
			frameList.add(new FrameObject());
			System.out.println("NEW FRAME ");
		}

		FrameObject lastFrame = frameList.get(frameList.size()-1);
		Thread.sleep(50);

		int count = 0;
		if(!lastFrame.checkLength()){
			count = lastFrame.append(data, 0);
			if(count < data.length - 1){
				FrameObject obj = new FrameObject(); 
				obj.append(data, count);
				frameList.add(obj);
			}			
		}
		System.out.println("##################################");
		for(byte b : frameList.get(frameList.size()-1).getFrame())
			System.out.print(b + " ");
		System.out.println();
		System.out.println("##################################");
		
		if(lastFrame.getCurrentFrameSize() > 3)
			System.out.println(lastFrame.getFrame().get(lastFrame.getCurrentFrameSize()-3) + " " 
					+lastFrame.getFrame().get(lastFrame.getCurrentFrameSize()-2) + " " + 
					lastFrame.getFrame().get(lastFrame.getCurrentFrameSize()-1));

		if(lastFrame.checkLength()){
			switch(lastFrame.getMessageID()){
			case 1:
				int obuID = lastFrame.getDeviceID();
				System.out.println(obuID);
				break;
			case 2:
				//					(RequestFrameObject)fo;
				break;
			case 3:
				break;
			case 4:
				addImageToStream(lastFrame);
				break;
			default:
				break;
			}
			return true;
		}
		for(byte b : lastFrame.getFrame())
			System.out.print(b + " ");
		System.out.println();
		System.out.println("FALSE FALSE FALSE " + lastFrame.checkLength());
		return false;
	}
	public boolean addImageToStream(FrameObject frame){

		try {
			switch(frame.getDatatype()){
			case 0:	// Bilddaten
				frame_count			= frame.getFrameCount();
				frame_offset		= frame.getFrameOffset();
				if(frame_count == 1)
					imageData = new ImageData();

				imageData.addData(frame.getFrameData());
				if(frame_offset == frame_count){
					//					imageData.writeImageToFile(ind++);
					//					StreamManager.getStream("BMW_I8").setImage(imageData.getData());
					//					this.videoStream.writeToStream(imageData.getBufferedImage());
					isCorrect = true;
				}
				else
					isCorrect = false;
				break;	
			case 1:	// Telemetriedaten
				TelemetryFrameObject tFrame = new TelemetryFrameObject(frame.getFrame());
				telemetry.addData(tFrame.getDataset());
				break;	
			case 2:
				break;
			}
			if(isCorrect){
				WebsocketResponse response = new WebsocketResponse(WebsocketResponse.TELEMETRY, imageData.getData(), telemetry);
				VideoStreamer streamer = StreamManager.getStream(5);
				if(streamer != null)
					streamer.setImage(response);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Write response to a File for Debug Issues.
	 * @param output	Response Data
	 */
	public void writeToFile1(){
		System.out.println("Write VideoStream");
		try {
			videoStream.getVideoStream();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
