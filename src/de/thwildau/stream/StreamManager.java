package de.thwildau.stream;

import java.util.HashMap;

public class StreamManager {
	
	public static HashMap<Integer, VideoStreamer> videoStreams = new HashMap<Integer, VideoStreamer>();
	
	public static void addStream(int id, VideoStreamer stream){
		videoStreams.put(id, stream);
	}

	public static VideoStreamer getStream(int id){
		return videoStreams.get(id);
	}
}
