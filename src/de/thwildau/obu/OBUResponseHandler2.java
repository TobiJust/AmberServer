package de.thwildau.obu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.thwildau.info.OBUMessage;
import de.thwildau.model.ImageData;
import de.thwildau.model.Telemetry;
import de.thwildau.stream.VideoStream;

public class OBUResponseHandler2 {

	int index = 0;
	private VideoStream videoStream;

	ImageData imageData = new ImageData();

	public OBUResponseHandler2(String name){
		videoStream = new VideoStream(0);
	}
	public boolean addImageToStream(byte[] response){
		System.out.println("Ready - " + response.length + " bytes");
		long startTime = System.currentTimeMillis();

		//		while((index = findFrame(response, OBUMessage.FRAME_BEGIN, index)) != -1){
		//
		//			imageData.addData(response);

		index = response.length-3;
		if(index == findFrame(response, OBUMessage.FRAME_END, response.length-3)){
			index += OBUMessage.FRAME_END.length;
			System.out.println("Transaction ready " + (System.currentTimeMillis() - startTime) + " ms");

//			imageData.addData(response);
//			imageData.addData(subarray(response, 0, response.length-4));
			return true;
		}
		else {

//			imageData.addData(response);
			//			throw new IllegalArgumentException("Frame end not found - Transaction cancelled");
			return false;
		}
		//		}
		//		imageData.addData(response);
		//		videoStream.writeToStream(imageData.getBufferedImage());
		//		imageData.writeImageToFile();

	}

	private int findFrame(byte[] input, byte[] frame, int offset){
		int c = 0;
		int d = offset;
		System.out.println((byte)input[offset]);

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
		try {
			videoStream.getVideoStream();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	public void writeImageToFile(){
		try {
			FileOutputStream os = new FileOutputStream(new File("imageFile.jpg"), false);
			os.write(imageData.getData());
			imageData.writeImageToFile();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
