package de.thwildau.obu;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

import de.thwildau.info.OBUMessage;
import de.thwildau.model.ImageData;
import de.thwildau.model.Telemetry;
import de.thwildau.obu.model.FrameObject;
import de.thwildau.obu.model.ImageFrameObject;
import de.thwildau.stream.VideoStream;

public class OBUResponseHandler {


	private LinkedList<FrameObject> frameList = new LinkedList<FrameObject>();
	//	private ArrayList<FrameObject> frameList = new ArrayList<FrameObject>();

	int index = 0;
	int length = 0;
	private VideoStream videoStream;
	private ByteArrayOutputStream inBuffer;
	private int frame_count;
	private int frame_offset;
	int n = 0;

	ImageData imageData;
	Telemetry telemetry;

	public OBUResponseHandler(String name){
		videoStream = new VideoStream(0);
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
		if(frameList.size() < 1 || frameList.get(frameList.size()-1).checkLength()) 
			frameList.add(new FrameObject());

		FrameObject lastFrame = frameList.get(frameList.size()-1);
		int count = 0;
		if(!lastFrame.checkLength()){
			count = lastFrame.append(data, 0);
			if(count < data.length - 1){
				FrameObject obj = new FrameObject(); 
				obj.append(data, count);
				frameList.add(obj);
			}			
		}
		if(lastFrame.checkLength()){
			for(FrameObject fo : frameList)
				switch(fo.getMessageID()){
				case 4:
					addImageToStream((ImageFrameObject) fo);
					break;
				}
			//			addImageToStream(frameList.get(frameList.size()-2));
			return true;
		}
		return false;
	}
	public boolean addImageToStream(ImageFrameObject frame){
		long startTime = System.currentTimeMillis();	

		try {
			System.out.println("FRAMES " + frame.getFrameOffset() + " | " + frame.getFrameCount());
			switch(frame.getMessageID()){
			case 3:	// Bilddaten
				frame_count			= frame.getFrameCount();
				frame_offset		= frame.getFrameOffset();
				if(frame_count == 1)
					imageData = new ImageData();

				imageData.addData(frame.getImageData());

				if(frame_offset == frame_count){
					//					imageData.writeImageToFile();
					this.videoStream.writeToStream(imageData.getBufferedImage());
				}
				break;	
			case 1:	// Telemetriedaten
				//				int data_index = 3;
				//				while(data_index < bodyLength(payload_length)){
				//					data_id				= message_body[data_index++];
				//					data_length			= message_body[data_index++];
				//					String data = "";
				//					System.out.print("Data " + data_id + ": ");
				//					for(int i = 0; i < data_length; i++){
				//						data += (char)message_body[data_index+i];
				//						System.out.print(data);
				//					}
				//					data_index += data_length;
				//					telemetry.addData(data_id, data);
				//					System.out.println();
				//				}
				break;	
			case 2:
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean addImageToStream(byte[] response){
		System.out.println("Ready - " + response.length + " bytes");
		long startTime = System.currentTimeMillis();

		ImageData imageData = new ImageData();
		Telemetry telemetry = new Telemetry();

		while((index = findFrame(response, OBUMessage.FRAME_BEGIN, index)) != -1){

			index += OBUMessage.FRAME_BEGIN.length;
			System.out.println(index + "  " + response[index]);
			byte[] payload_length 	= subarray(response, index, ++index); 
			if((bodyLength(payload_length)+index-1) > response.length)
				return false;
			byte[] message_body		= subarray(response, ++index, bodyLength(payload_length)+index-1);
			byte id 				= message_body[0];
			byte obu_id				= message_body[1];
			byte flag				= message_body[2];
			byte frame_count = 0;
			byte frame_offset = 0;
			byte data_id;
			byte data_length;
			System.out.println("####### ID: " + id + "  OBU_ID: " + obu_id + " ####### Length: " + (bodyLength(payload_length)));
			switch(flag){
			case 0:	// Bilddaten
				frame_count			= message_body[3];
				frame_offset		= message_body[4];
				//				imageData.addData(subarray(message_body, 5, bodyLength(payload_length)));
				break;	
			case 1:	// Telemetriedaten
				int data_index = 3;
				while(data_index < bodyLength(payload_length)){
					data_id				= message_body[data_index++];
					data_length			= message_body[data_index++];
					String data = "";
					System.out.print("Data " + data_id + ": ");
					for(int i = 0; i < data_length; i++){
						data += (char)message_body[data_index+i];
						System.out.print(data);
					}
					data_index += data_length;
					telemetry.addData(data_id, data);
					System.out.println();
				}
				break;	
			case 2:
				break;
			case 3:
				break;
			}
			index += bodyLength(payload_length);
			byte checksum			= response[index++];	
			if(checksum == checksum(message_body))
				System.out.println("Data correct");
			if(index == findFrame(response, OBUMessage.FRAME_END, index)){
				index += OBUMessage.FRAME_END.length;
				System.out.println("--------- Frame " +frame_offset + " / " + frame_count + " ---------");	
				if(frame_offset == frame_count)
					return true;
			}
			else {
				//				throw new IllegalArgumentException("Frame end not found - Transaction cancelled");
				return false;
			}
		}
		System.out.println("Transaction ready " + (System.currentTimeMillis() - startTime) + " ms");
		//		imageData.addData(response);
		videoStream.writeToStream(imageData.getBufferedImage());
		//		imageData.writeImageToFile();

		return true;
	}

	private int findFrame(byte[] input, byte[] frame, int offset){
		int c = 0;
		int d = offset;

		if(offset < 0)
			return -1;
		while(c < frame.length  && d < input.length){
			if(input[d] == frame[c])
				c++;
			else
				c=0;
			d++;
		}
		if (c == (frame.length)){
			return d - frame.length;
		}
		return -1;
	}
	/**
	 * Get a sub array of a large array from start index to end index.
	 * 
	 * @param arr	Large Array for lookup
	 * @param start	Index to start the sub
	 * @param end	Index to end the sub
	 * @return Subarray of the larger one.
	 */
	private byte[] subarray(byte[] arr, int start, int end){
		byte[] sub = new byte[end-start+1];
		int i = 0;
		while(i < sub.length){
			sub[i] = arr[start+i];
			i++;
		}
		return sub;
	}
	/**
	 * Convert payload length to integer value.
	 * 
	 * @param b	Payload length Array (2 Bytes)
	 * @return	Integer value of payload length.
	 */
	public final int bodyLength(byte[] b){
		int i = 0;
		i |= b[0] & 0xFF;
		i <<= 8;
		i |= b[1] & 0xFF;
		return i;
	}

	private byte checksum(byte[] content) {
		byte checksum = content[0];
		for(int i = 1; i < content.length; i++){
			checksum ^= content[i];
		}
		return checksum;
	}

	/**
	 * Write response to a File for Debug Issues.
	 * @param output	Response Data
	 */
	public void writeToFile(){
		System.out.println("Write VideoStream");
		try {
			videoStream.getVideoStream();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	public void writeImageToFile(){
		//		try {
		//			FileOutputStream os = new FileOutputStream(new File("imageFile.jpg"), false);
		//			os.write(imageData.getData());
		//			imageData.writeImageToFile();
		//		} catch (FileNotFoundException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

	}
}
