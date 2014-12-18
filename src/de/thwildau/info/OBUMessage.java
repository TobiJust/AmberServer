package de.thwildau.info;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OBUMessage {

	public final static byte[] FRAME_BEGIN = {(byte)0xFF, (byte)0x00, (byte)0xFF};
	public final static byte[] FRAME_END	 = {(byte)0xFF, (byte)0x00, (byte)0xFF};

	public final static byte REQUEST_PICTURE	= (byte) 0x01;
	public final static byte REQUEST_TELEMETRY	= (byte) 0x02;
	public final static byte CONTROL_COMMAND	= (byte) 0x03;
	public final static byte REQUEST_DATA		= (byte) 0x05;

	public byte[] request;
	private String info;
	
	int index = 0;

	/**
	 * Message for Request to OBU.
	 * 
	 * @param id	Request id
	 * @param content	Content of the Request
	 */
	public OBUMessage(byte id, byte[] content){
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			// Frame begin
			output.write(FRAME_BEGIN);
			// Payload Length
			output.write(payload(content));
			// Message Body
			output.write(id);	// Message ID
			output.write(content);
			// Checksum
			output.write(checksum(id, content));
			// Frame end
			output.write(FRAME_END);

			this.request = output.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] payload(byte[] content) {
		int payload_Length = 1 + content.length; // content.length + id (1 byte)
		byte[] payload = new byte[2];
		payload[0] = (byte)(payload_Length>>8);
		payload[1] = (byte)payload_Length;

		return payload;
	}

	private byte checksum(byte id, byte[] content) {
		byte checksum = (byte) (id ^ content[0]);
		for(int i = 1; i < content.length; i++)
			checksum ^= content[i];

		return checksum;
	}

	public String getInfo(){
		return this.info;
	}	
}
