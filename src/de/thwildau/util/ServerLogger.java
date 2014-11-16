package de.thwildau.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerLogger{

	private static FileWriter logger;

	public static void init(boolean print) throws Exception{
		
		if(logger!=null)
			throw new Exception("Logger already running...");
		

		Logger.getLogger("org.eclipse.jetty").setLevel(Level.WARNING);		
		logger = new FileWriter(new File(new SimpleDateFormat("yyyy-MM-dd").format(new Date())+".log"), true);
		String msg = "\n"+timeStamp()+": Logger initialized...";
		logger.append(msg);
		if(print)
			System.out.println(msg);
	}

	public static void log(String message, boolean print){
		if(logger == null){
			System.err.println("Need to initialize logger first");
			return;
		}
		String msg = "\n"+timeStamp()+": "+message;
		try {
			logger.append(msg);
		} catch (IOException e) {
			try {
				init(false);
				logger.append(msg);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			e.printStackTrace();
		}
		if(print)
			System.out.println(msg);
	}

	public static void stop(boolean print){
		try{
			if(logger!=null){
				String msg = "\n\n\n"+timeStamp()+": Logger shutdown...";
				logger.append(msg);
				if(print)
					System.out.println(msg);
				logger.close();
				logger=null;
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private static String timeStamp(){
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}

}