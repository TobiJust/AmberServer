package de.thwildau.info;

import java.io.Serializable;

public class OBUMessage2 implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1463554700663347900L;
	private String info;
	private byte[] b;
	
	public OBUMessage2(byte[] b){
		this.b = b;
	}
	public OBUMessage2(String info){
		this.info = info;
	}
	
	public String getInfo(){
		return this.info;
	}
	public byte[] getByte() {
		return this.b;
	}

}
