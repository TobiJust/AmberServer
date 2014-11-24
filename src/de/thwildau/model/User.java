package de.thwildau.model;

import java.io.Serializable;

public class User implements Serializable{
	
	private static final long serialVersionUID = -3989078864083454472L;
	private String userName;
	private byte[] pass;
	private String regID;

	public String getName() {		
		return this.userName;
	}

	public byte[] getPass() {
		return this.pass;
	}

	public String getRegistationID(){
		return this.regID;
	}
	
}
