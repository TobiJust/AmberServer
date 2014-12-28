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

	/*
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
		if (decoderState == null) {
			decoderState = new DecoderState();
			session.setAttribute(DECODER_STATE_KEY, decoderState);
		}
		if (decoderState.image1 == null) {
			// try to read first image
			if (in.prefixedDataAvailable(4, MAX_IMAGE_SIZE)) {
				decoderState.image1 = readImage(in);
			} else {
				// not enough data available to read first image
				return false;
			}
		}
		if (decoderState.image1 != null) {
			// try to read second image
			if (in.prefixedDataAvailable(4, MAX_IMAGE_SIZE)) {
				BufferedImage image2 = readImage(in);
				ImageResponse imageResponse = new ImageResponse(decoderState.image1, image2);
				out.write(imageResponse);
				decoderState.image1 = null;
				return true;
			} else {
				// not enough data available to read second image
				return false;
			}
		}
		return false;
	}
	 */
	private BufferedImage readImage(IoBuffer in) throws IOException {
		int length = in.getInt();
		System.out.println("Length " + length);
		byte[] bytes = new byte[length];
		in.get(bytes);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		return ImageIO.read(bais);
	}

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		//Write your business logic here how to translate a data-packet into what the server understands,
		//NOTE it is highly possible that the data is not complete in 1 packet and might required a re-combination
		//If packet is not complete return false to reiterate with next packet. ELse return true if packet is complete
		//I have mentioned a sample code

		DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);

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
				for (int i=in.position(); i< in.limit(); i++) {
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