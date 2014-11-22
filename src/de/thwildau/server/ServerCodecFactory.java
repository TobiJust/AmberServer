package de.thwildau.server;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.textline.TextLineEncoder;

public class ServerCodecFactory implements ProtocolCodecFactory {
	/**
	    * A protocol without encoder and docoder is a waste. 
	    * Encode mentioned here
	    */
	    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
	        return new ServerResponseEncoder();
	    }
	    
	    /**
	     * A protocol without encoder and docoder is a waste. 
	     * Encode mentioned here
	     */
	    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
	        return new ServerRequestDecoder();
	    }
}