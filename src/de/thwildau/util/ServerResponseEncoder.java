package de.thwildau.util;

import java.io.NotSerializableException;
import java.io.Serializable;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
/**
 * 
 * This class contains the logic how to encode a data-packet to transmit it to the clients.
 * It both supports messages for serializable objects and byte arrays.
 * 
 * @author Tobias Just
 *
 */
public class ServerResponseEncoder extends ProtocolEncoderAdapter {

	private int maxObjectSize = Integer.MAX_VALUE; // 2GB

	public void encode(IoSession ioSession, Object message, ProtocolEncoderOutput out) throws Exception {

		if (!(message instanceof Serializable)) {
			throw new NotSerializableException();
		}
		
		IoBuffer buf = IoBuffer.allocate(0);
		buf.setAutoExpand(true);
		if(message instanceof byte[])
			buf.put((byte[])message);
		else
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