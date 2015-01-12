package de.thwildau.obu;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;

import de.thwildau.gcm.SendNotification;
import de.thwildau.model.Event;
import de.thwildau.model.ImageData;
import de.thwildau.model.Telemetry;
import de.thwildau.obu.model.EventFrameObject;
import de.thwildau.obu.model.FrameObject;
import de.thwildau.obu.model.TelemetryFrameObject;
import de.thwildau.stream.StreamManager;
import de.thwildau.stream.VideoStreamer;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;
import de.thwildau.webserver.WebsocketResponse;

/**
 * This is the handler which manage incoming data from the Onboard Unit.
 * It will add data to a frame object until it is complete, otherwise send
 * a return value, which will init a new request for data to the obu.
 * 
 * @author Tobias Just
 * @version 1.0
 * @date 11.01.2015
 *
 */
public class OBUFrameHandler {
	/**
	 * List of all frames received from the OBU.
	 */
	private LinkedList<FrameObject> frameList = new LinkedList<FrameObject>();
	/**
	 * The HashMap which manage MINA sessions by obu id.
	 */
	public static ConcurrentHashMap<Integer, IoSession> handlers = new ConcurrentHashMap<Integer, IoSession>();
	/**
	 * The HashMap which manage OBUFrameHandler Objects by a MINA session.
	 */
	public static ConcurrentHashMap<IoSession, OBUFrameHandler> obuHandlers = new ConcurrentHashMap<IoSession, OBUFrameHandler>();
	/**
	 *  Frame variables
	 */
	private int frame_count;
	private int frame_offset;

	/**
	 *  Objects to fill up data
	 */
	private ImageData imageData;
	private Telemetry telemetry = new Telemetry();
	private Event event = new Event();

	/**
	 *  Check whether the image frame is complete
	 */
	private boolean isCorrect;
	/**
	 * Current OBU id. (5 by default)
	 */
	private int obuID = 5;

	/**
	 * Add data till the Frame is complete. Otherwise create a new Frame.
	 * 
	 * @param data Data received from OBU
	 * @return true, if Frame is complete
	 * 		   false, if Frame needs more Data
	 * @throws Exception	If it want add data to frame, which size is smaller then the index to set.
	 */
	public boolean addData(byte[] data, IoSession session) throws Exception{	
		long current = System.currentTimeMillis();
		// Create new Frame Object if list is empty or last frame is correct
		if(frameList.size() < 1 || frameList.getLast().checkLength()){
			frameList.add(new FrameObject());
		}
		// get last frame to add new data
		FrameObject lastFrame = frameList.getLast();

		// Count variable for adding new data on the right index
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

		// interpret all current frames by their message id
		try {
			FrameObject tmp = null;
			do{
				tmp = frameList.getFirst();
				switch(tmp.getMessageID()){
				case FrameObject.ACK:
					obuID = tmp.getDeviceID();
					handlers.put(obuID, session);
					ServerLogger.log("Process duration OBU ID " + (System.currentTimeMillis()-current), Constants.DEBUG);
					break;
				case FrameObject.STREAM:
					handleFrameObject(tmp);
					ServerLogger.log("Process duration STREAM FRAME " + (System.currentTimeMillis()-current), Constants.DEBUG);	
					break;
				case FrameObject.EVENT:
					System.out.println("#####################################");
					System.out.println("#####################################");
					System.out.println("#####################################");
					System.out.println("#####################################");
					handleEventFrameObject(tmp);
					ServerLogger.log("Process duration EVENT FRAME " + (System.currentTimeMillis()-current), Constants.DEBUG);
					break;
				default:
					break;
				}
				// remove recently touched frames
				frameList.removeFirst();

				// if frame is correct, means frame offset is equal to frame count,
				// build a response for the web socket by adding image and telemetry data
				if(isCorrect){
					WebsocketResponse response = new WebsocketResponse(WebsocketResponse.TELEMETRY, imageData.getData(), telemetry, event);
					// get the streamer by obu id
					VideoStreamer streamer = StreamManager.getStream(obuID);

					// if there is no streamer, create a new one
					if(streamer == null){
						streamer = new VideoStreamer();
						StreamManager.addStream(obuID, streamer);
					}
					// set response for the web socket thread
					streamer.setResponseData(response);
					ServerLogger.log("Process duration TOTAL " + (System.currentTimeMillis()-current), Constants.DEBUG);
				}
			}
			while(tmp != null && tmp.checkLength());
		} catch (NoSuchElementException e) { }

		return true;
	}
	/**
	 * This method handles a stream frame to seperate it by image
	 * and telemetry data. It collects data to an object, until 
	 * the image is complete. 
	 * To collect telemetry data, interpret the dataset id by id and
	 * add the suitable data.
	 * 
	 * @param frame	Frame of a data stream from the obu.
	 */
	public void handleFrameObject(FrameObject frame){

		try {
			switch(frame.getDatatype()){
			case 0:	// image data
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
			case 1:	// telemetry data
				TelemetryFrameObject tFrame = new TelemetryFrameObject(frame.getFrame());
				telemetry.addData(tFrame.getDataset());
				break;	
			case 2:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * This method handles a event frame to seperate it by image
	 * and event data. It collects data to an object, until 
	 * the image is complete.
	 * To collect telemetry data, interpret the dataset id by id and
	 * add the suitable data. 
	 * 
	 * @param frame	Frame of a data stream from the obu.
	 */
	public void handleEventFrameObject(FrameObject frame){
		try {
			switch(frame.getDatatype()){
			case 0:	// Event Image
				frame_count			= frame.getFrameCount();
				frame_offset		= frame.getFrameOffset();
				if(frame_count == 1)
					imageData = new ImageData();

				imageData.addData(frame.getFrameData());
				if(frame_offset == frame_count)
					event.setEventImage(imageData.getData());
				break;	
			case 1:	// Event Data
				EventFrameObject eFrame = new EventFrameObject(frame.getFrame());
				event.setEventType(eFrame.getEventIdentifier());
				System.out.println("IDENTIFIER " + eFrame.getEventIdentifier());
				System.out.println("IDENTIFIER " + eFrame.getEventIdentifier());
				System.out.println("IDENTIFIER " + eFrame.getEventIdentifier());
				event.addData(eFrame.getDataset());
				break;	
			}
			// send notification to observers
			if(event.getEventType().length() > 0)
				new SendNotification(event, obuID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
