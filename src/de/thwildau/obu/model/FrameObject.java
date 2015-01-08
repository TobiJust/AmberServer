package de.thwildau.obu.model;

import java.util.ArrayList;
import java.util.List;

public class FrameObject {

	protected static final int PAYLOAD_L = 4;
	protected static final int PAYLOAD_H = 3;
	public static final byte DEVICE = 9;
	private ArrayList<Byte> frame;

	public FrameObject(){
		frame = new ArrayList<Byte>();
	}

	public boolean append(byte b){
		if(frame.size() + 1 <= bodyLength() + 9){
			frame.add(b);
			return true;
		}
		return false;
	}
	public int append(byte[] arr, int pos){
		int counter = pos;
		while(counter < arr.length && append(arr[counter++]));

		return counter-1;
	}

	public boolean checkLength(){
		if(!checkFrameBegin() || frame.size() < 5){
			System.out.println("checkFrameBegin() " + checkFrameBegin() + " FRAME SIZE " + frame.size());
			return false;
		}
		int length = bodyLength();
		if(frame.size() == length+9 )
			return true;
		return false;
	}

	public boolean checkFrameBegin(){
		if(frame.size() < 3)
			return false;
		return frame.get(0) == (byte)0xFF && frame.get(1) == (byte)0x00 && frame.get(2) == (byte)0xFF;
	}
	public boolean checkFrameEnd(int i){
		int size = frame.size();
		if(size < 3)
			return false;

		return frame.get(i) == (byte)0xFF && frame.get(i+1) == (byte)0x00 && frame.get(i+2) == (byte)0xFF;
	}
	/**
	 * Convert payload length to integer value.
	 * 
	 * @param b	Payload length Array (2 Bytes)
	 * @return	Integer value of payload length.
	 */
	public int bodyLength(){
		if(frame.size() < PAYLOAD_L + 1)
			return -1;
		int i = 0;
		i |= frame.get(PAYLOAD_H) & 0xFF;
		i <<= 8;
		i |= frame.get(PAYLOAD_L) & 0xFF;
		return i;
	}
	public int checksum() throws Exception{
		byte checksum = 0;
		for(byte b : getChecksumData()){
			checksum ^= b;
		}
		return checksum;
	}
	public int getMessageID() throws Exception{
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + 5 + " Frame size: " + frame.size());

		return frame.get(5);			
	}
	public int getChecksum() throws Exception{ 
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + (bodyLength()+6) + " Frame size: " + frame.size());

		return frame.get(frame.size()-4);		
	}

	public int getCurrentFrameSize(){
		return frame.size();
	}

	public int getDeviceID() throws Exception{
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + 6 + " Frame size: " + frame.size());

		return frame.get(6) & 0xFF;	
		//		return "123";
	}
	public int getDatatype() throws Exception{
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + 7 + " Frame size: " + frame.size());

		return frame.get(7);			
	}
	public int getFrameCount() throws Exception{
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + 8 + " Frame size: " + frame.size());

		return frame.get(8);			
	}
	public int getFrameOffset() throws Exception{
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + 9 + " Frame size: " + frame.size());

		return frame.get(9);			
	}
	public List<Byte> getChecksumData() throws Exception{
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + (bodyLength()+5) + " Frame size: " + frame.size());

		return frame.subList(5, bodyLength()+5);	
	}
	public List<Byte> getFrameData() throws Exception{ 
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + (bodyLength()+5) + " Frame size: " + frame.size());

		return frame.subList(10, bodyLength()+5);		
	}

	public ArrayList<Byte> getFrame() {
		return frame;
	}



}
