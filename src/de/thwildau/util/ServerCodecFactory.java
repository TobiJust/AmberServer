package de.thwildau.util;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * 
 * @author Tobias Just
 *
 * This class contains factory methods to create new objects for encoding and decoding
 * communication data.
 */
public class ServerCodecFactory implements ProtocolCodecFactory {
	/**
	 * A protocol encoder to encode outgoing data. 
	 * It returns a new ServerResponseDecoder.
	 */
	public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
		return new ServerResponseEncoder();
	}

	/**
	 * A protocol decoder to decode incoming data. 
	 * It returns a new ServerRequestDecoder.
	 */
	public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
		return new ServerRequestDecoder();
	}
}