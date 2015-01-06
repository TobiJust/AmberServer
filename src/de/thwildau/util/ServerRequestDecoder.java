package de.thwildau.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import de.thwildau.info.ClientMessage;


public class ServerRequestDecoder extends CumulativeProtocolDecoder {

	private ByteArrayOutputStream os = new ByteArrayOutputStream();

	private static final String DECODER_STATE_KEY = ServerRequestDecoder.class.getName() + ".STATE";

	public static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024;

	private static class DecoderState {
		BufferedImage image1;
	}

	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		//Write your business logic here how to translate a data-packet into what the server understands,
		//NOTE it is highly possible that the data is not complete in 1 packet and might required a re-combination
		//If packet is not complete return false to reiterate with next packet. ELse return true if packet is complete
		//I have mentioned a sample code


		in.setAutoExpand(true);
		in.setAutoShrink(true);

		try{
			ClientMessage msg = (ClientMessage) in.getObject();

			out.write(msg);
			return true;
		}catch(Exception e){
			byte[] byteArray = null;
			while (in.hasRemaining()) {
				byteArray = new byte[in.limit()];
				for (int i=in.position(); i < in.limit(); i++) {
					byte b = in.get(i);
					byteArray[i] = b;
				}
				in.position(in.limit());
				out.write(byteArray);
				return true;
			}
			return false;
		}

	}

}