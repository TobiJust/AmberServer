package de.thwildau.server;

import java.io.NotSerializableException;
import java.io.Serializable;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import de.thwildau.info.ClientMessage;

public class ServerResponseEncoder extends ProtocolEncoderAdapter {

	private static final String RESPONSE_DELIMITER_CHAR = "";
	private int maxObjectSize = Integer.MAX_VALUE; // 2GB

	public void encode(IoSession ioSession, Object message, ProtocolEncoderOutput out) throws Exception {

		if (!(message instanceof Serializable)) {
			throw new NotSerializableException();
		}
		System.out.println("Message");
		System.out.println(message.toString().length());

		
		IoBuffer buf = IoBuffer.allocate(128);
		buf.setAutoExpand(true);
		buf.putObject(message);

		int objectSize = buf.position() - 4;
		if (objectSize > maxObjectSize) {
			buf.shrink();
			throw new IllegalArgumentException(
					"The encoded object is too big: " + objectSize + " (> "
							+ maxObjectSize + ')');
		}

		buf.flip();
		out.write(buf);
		System.out.println(buf);
	}

	/**
	 * Returns the allowed maximum size of the encoded object.
	 * If the size of the encoded object exceeds this value, this encoder
	 * will throw a {@link IllegalArgumentException}.  The default value
	 * is {@link Integer#MAX_VALUE}.
	 */
	public int getMaxObjectSize() {
		return maxObjectSize;
	}
}