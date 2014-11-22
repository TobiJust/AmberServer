package de.thwildau.server;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import de.thwildau.info.ClientMessage;


public class ServerRequestDecoder extends CumulativeProtocolDecoder {

	private static final char REQUEST_DELIMITER_CHAR = '\n';

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		//Write your business logic here how to translate a data-packet into what the server understands,
		//NOTE it is highly possible that the data is not complete in 1 packet and might required a re-combination
		//If packet is not complete return false to reiterate with next packet. ELse return true if packet is complete
		//I have mentioned a sample code

		in.setAutoExpand(true);
		in.setAutoShrink(true);

		System.out.println(in.remaining());

		//		if (in.remaining() >= 12) {
		try{
			ClientMessage msg = (ClientMessage) in.getObject();

			out.write(msg);
			return true;
		}catch(Exception e){

			while (in.hasRemaining()) {
				char testChar = (char)in.get(in.limit()-1);
				System.out.println("Message Size: (incl. limitChar) " + in.limit());
				System.out.println(">>>" +testChar +"<<<");
				char[] charArray = new char[in.limit() - in.position()];
				for (int i=in.position(); i< in.limit(); i++) {
					char ch = (char)in.get(i);
					charArray[i] = ch;
				}
				in.position(in.limit());
				String outputString = new String(charArray);
				out.write(outputString);
				return true;
			}
			return false;
		}
	}

}