package de.thwildau.stream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.thwildau.util.Constants;

/**
 * 
 * This class just contains a Hash Map to hold video
 * 
 * @author Tobias Just
 *
 */
public class StreamManager {

	public static HashMap<Integer, VideoStreamer> videoStreams = new HashMap<Integer, VideoStreamer>();
	private static PrintWriter writer;
	private static boolean print = false;
	public static void addStream(int id, VideoStreamer stream){
		if(!videoStreams.containsKey(id))
			videoStreams.put(id, stream);
	}

	public static VideoStreamer getStream(int id){
		return videoStreams.get(id);
	}

	public static void init(){
		String logFileName = new SimpleDateFormat("yyyy-MM-dd'.log'").format(new Date());
		Path path = Paths.get(Constants.LOG_FOLDER + ""+logFileName);
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(path.toString(), true)));
			Runnable run = new Runnable() {
				public void run() {
					try {
						while (true) {
							Thread.sleep(1000);
							print = true;
						}
					} catch (InterruptedException e) {
						System.out.println(" interrupted");
					}
				}
			};
			new Thread(run).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void log(String text){
		try {
			System.out.println(print);
			if(print){
				String msg = "\n"+timeStamp()+": "+text;
				writer.print(msg);
				print = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stop(){
		writer.close();
	}
	private static String timeStamp(){
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
}
