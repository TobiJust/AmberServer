package de.thwildau.stream;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

import de.thwildau.model.Telemetry;
import de.thwildau.server.AmberServer;
import de.thwildau.util.Constants;
import de.thwildau.webserver.WebsocketResponse;

public class VideoStream extends Thread{

	private static final int FRAME_RATE = 30;

	private HashMap<Integer, IMediaWriter> streams = new HashMap<Integer, IMediaWriter>();

	private String outputFilename = "debug.mp4";

	private IMediaWriter writer;
	private long startTime;

	private BufferedImage image;

	private VideoStreamer streamer;
	private int userID;

	private boolean running = false;


	public VideoStream(VideoStreamer streamer, int userID, int vehicleID) {

		this.streamer = streamer;
		this.userID = userID;
		// build path and filename
		Path path = Paths.get(Constants.DATA_FOLDER+"//"+userID+"//"+vehicleID);
		if (!Files.exists(path)) (new File(path.toString())).mkdirs();	
		outputFilename = path.toString()+"//"+new SimpleDateFormat("yyyy-MM-dd HH-mm-ss'.mp4'").format(new Date());
		//		outputFilename = "debug.mp4";


		// let's make a IMediaWriter to write the file.
		writer = ToolFactory.makeWriter(outputFilename);
		System.out.println(writer);


		// We tell it we're going to add one video stream, with id 0,
		// at position 0, and that it will have a fixed frame rate of FRAME_RATE.
		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 
				320, 240);

		startTime = System.nanoTime();
		streams.put(0, writer);
	}
	public static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {

		BufferedImage image;

		// if the source image is already the target type, return the source image
		if (sourceImage.getType() == targetType) {
			image = sourceImage;
		}
		// otherwise create a new image of the target type and draw the new image
		else {
			image = new BufferedImage(sourceImage.getWidth(), 
					sourceImage.getHeight(), targetType);
			image.getGraphics().drawImage(sourceImage, 0, 0, null);
		}

		return image;
	}

	public void startRecord() {
		this.running = true;
		start();
	}
	/**
	 * Tell the writer to close and write the video to the file system.
	 */
	public void getVideoStream(){
		this.running  = false;
		streams.get(0).close();

		AmberServer.getDatabase().storeVideostream(userID, outputFilename);
//		try {
//			new VideoDownload(outputFilename).doHttpUrlConnectionAction();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	/**
	 * Add single images to a stream writer to decode a video.
	 * This method also add telemetry data as text on the video images.
	 */
	public void writeToStream(WebsocketResponse response) {
		if(response == null)
			return;

		byte[] imageInByte = response.getImage();
		Telemetry telemetry = (Telemetry) response.getData();

		try {
			// convert byte array to BufferedImage
			InputStream in = new ByteArrayInputStream(imageInByte);
			image = ImageIO.read(in);
			// convert to the right image type
			BufferedImage bgrScreen = convertToType(image, 
					BufferedImage.TYPE_3BYTE_BGR);

			// get the graphics for the image
			Graphics2D g = bgrScreen.createGraphics();

			// add text to the picture
			g.setColor(Color.WHITE);
			g.setFont(new Font("default", Font.BOLD, 20));
			
//			g.drawString(telemetry.toString(), 0, bgrScreen.getHeight()-20);
			g.drawString("Speed " + telemetry.getSpeed()+Math.random(), 50, bgrScreen.getHeight()-20);
			g.drawString("Longitude "+telemetry.getLon(), 50, bgrScreen.getHeight()-45);
			g.drawString("Latitude "+telemetry.getLat(), 50, bgrScreen.getHeight()-70);
			g.drawString("Fuel "+telemetry.getFuel(), 50, bgrScreen.getHeight()-95);

			// encode the image to stream #0
			writer.encodeVideo(0, bgrScreen, System.nanoTime() - startTime, 
					TimeUnit.NANOSECONDS);

			try {
				Thread.sleep((long) (1000 / FRAME_RATE));
			} 
			catch (InterruptedException e) {
				// ignore
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		while(running){
			writeToStream(this.streamer.getResponse());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}