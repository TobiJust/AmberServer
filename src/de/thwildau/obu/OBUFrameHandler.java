package de.thwildau.obu;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;

import de.thwildau.model.Event;
import de.thwildau.model.ImageData;
import de.thwildau.model.Telemetry;
import de.thwildau.obu.model.FrameObject;
import de.thwildau.obu.model.TelemetryFrameObject;
import de.thwildau.stream.StreamManager;
import de.thwildau.stream.VideoStreamer;
import de.thwildau.webserver.WebsocketResponse;

/**
 * 
 * @author Tobias Just
 *
 */
public class OBUFrameHandler {

	// List of all frames received from the OBU
	private LinkedList<FrameObject> frameList = new LinkedList<FrameObject>();
	public static ConcurrentHashMap<Integer, IoSession> handlers = new ConcurrentHashMap<Integer, IoSession>();
	public static ConcurrentHashMap<IoSession, OBUFrameHandler> obuHandlers = new ConcurrentHashMap<IoSession, OBUFrameHandler>();

	// Frame
	private int frame_count;
	private int frame_offset;

	// Objects to fill up data
	private ImageData imageData;
	private Telemetry telemetry = new Telemetry();
	private Event event;

	// Check whether the image frame is complete
	private boolean isCorrect;


	/**
	 * Add data till the Frame is complete. Otherwise create a new Frame.
	 * 
	 * @param data Data received from OBU
	 * @return true, if Frame is complete
	 * 		   false, if Frame needs more Data
	 * @throws Exception 
	 */
	public boolean addData(byte[] data) throws Exception{	

		System.out.println("data " + data);
		for(byte b : data){
			System.out.print(b + " ");
		}
		System.out.println();
		// Create new Frame Object if list is empty or last frame is correct
		if(frameList.size() < 1 || frameList.getLast().checkLength()){
			frameList.add(new FrameObject());
		}

		// get last frame to add new data
		FrameObject lastFrame = frameList.getLast();

		int count = 0;
		if(!lastFrame.checkLength()){
			count = lastFrame.append(data, 0);
			while(count < data.length - 1){
				FrameObject obj = new FrameObject(); 
				count = obj.append(data, count);
				frameList.add(obj);
			}			
		}
		// if last frame is not correct return false, to collect more data
		if (!frameList.getLast().checkLength())
			return false;

		// interpret frame
		try {
			FrameObject tmp = null;
			do{
				tmp = frameList.getFirst();
				switch(tmp.getMessageID()){
				case 1:
					int obuID = tmp.getDeviceID();
					System.out.println(obuID);
					break;
				case 2:
					break;
				case 3:
					break;
				case 4:
					handleFrameObject(tmp);
					break;
				case 5:
					handleEventFrameObject(tmp);
					break;
				default:
					break;
				}
				frameList.removeFirst();
				if(isCorrect){
					WebsocketResponse response = new WebsocketResponse(WebsocketResponse.TELEMETRY, imageData.getData(), telemetry, event);
					VideoStreamer streamer = StreamManager.getStream(5);
					if(streamer != null)
						streamer.setResponseData(response);
				}
			}
			while(tmp != null && tmp.checkLength());
		} catch (NoSuchElementException e) { }

		return true;
	}
	/**
	 * 
	 * @param frame
	 * @return
	 */
	public boolean handleFrameObject(FrameObject frame){

		try {
			switch(frame.getDatatype()){
			case 0:	// Bilddaten
				frame_count			= frame.getFrameCount();
				frame_offset		= frame.getFrameOffset();
				if(frame_count == 1)
					imageData = new ImageData();

				imageData.addData(frame.getFrameData());
				if(frame_offset == frame_count){
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


		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean handleEventFrameObject(FrameObject frame){
		
		return false;
	}
}
