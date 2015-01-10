package de.thwildau.obu.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TelemetryFrameObject extends FrameObject{
	

	private ArrayList<Byte> frame;

	public TelemetryFrameObject(ArrayList<Byte> data){
		this.frame = new ArrayList<Byte>();
		frame = data;
	}

	public int getCurrentFrameSize(){
		return frame.size();
	}
	/**
	 * Convert payload length to integer value.
	 * 
	 * @param b	Payload length Array (2 Bytes)
	 * @return	Integer value of payload length.
	 */
	public final int bodyLength(){
		if(frame.size() < PAYLOAD_L + 1)
			return -1;
		int i = 0;
		i |= frame.get(PAYLOAD_H) & 0xFF;
		i <<= 8;
		i |= frame.get(PAYLOAD_L) & 0xFF;
		return i;
	}
	public int checksum() throws Exception{
		byte checksum = getFrameData().get(0);
		for(byte b : getFrameData()){
			checksum ^= b & 0xFF;
		}
		return checksum & 0xFF;
	}
	public boolean checkLength(){
		if(!checkFrameBegin() || frame.size() < 5)
			return false;

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
	public int getChecksum() throws Exception{ 
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + (bodyLength()+6) + " Frame size: " + frame.size());

		return frame.get(frame.size()-4) & 0xFF;		
	}
	public boolean checkFrameEnd(){
		int size = frame.size();
		if(size < 3)
			return false;

		return frame.get(size-3) == (byte)0xFF && frame.get(size-2) == (byte)0x00 && frame.get(size-1) == (byte)0xFF;
	}

	public int getDataID(int index) throws Exception{
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + 6 + " Frame size: " + frame.size());

		return frame.get(index);			
	}
	public int getDataLength(int index) throws Exception{
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + 7 + " Frame size: " + frame.size());

		return frame.get(index) & 0xFF;			
	}
	public HashMap<Byte, List<Byte>> getDataset() throws Exception{
		HashMap<Byte, List<Byte>> data = new HashMap<Byte, List<Byte>>();
		int index = 8;
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + index + " Frame size: " + frame.size());
		while(index < bodyLength()+3){
			data.put(frame.get(index), frame.subList(index+2, frame.get(index+1) + (index+2)));
			index = frame.get(index+1) + (index+2);
		}

		//		return frame.subList(index, length+index);	
		return data;
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
