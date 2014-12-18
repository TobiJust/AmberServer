package de.thwildau.stream;

import java.util.HashMap;

public class StreamManager {
	
	public static HashMap<String, VideoStreamer> videoStreams = new HashMap<String, VideoStreamer>();
	
	public static void addStream(String id, VideoStreamer stream){
		videoStreams.put(id, stream);
	}

	public static VideoStreamer getStream(String id){
		return videoStreams.get(id);
	}
}
