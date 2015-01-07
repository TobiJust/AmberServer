package de.thwildau.obu.model;

import java.util.ArrayList;
import java.util.List;

public class RequestFrameObject extends FrameObject{

	private ArrayList<Byte> frame;

	public RequestFrameObject(){
		frame = new ArrayList<Byte>();
	}
	public String getDeviceID() throws Exception{
		if(!checkLength())
			throw new Exception("Out Of Bounds Exception - Index: " + 6 + " Frame size: " + frame.size());

//		return frame.get(6);
		return "123";
	}
}
