package de.thwildau.obu.model;

import java.util.ArrayList;
import java.util.List;

public class ImageFrameObject extends FrameObject{

	private ArrayList<Byte> frame;

	public ImageFrameObject(){
		frame = new ArrayList<Byte>();
	}
	public int getDeviceID() throws Exception{
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + 6 + " Frame size: " + frame.size());

		return frame.get(6);			
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
	public List<Byte> getImageData() throws Exception{ 
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + (bodyLength()+5) + " Frame size: " + frame.size());

		return frame.subList(10, bodyLength()+5);		
	}



}
