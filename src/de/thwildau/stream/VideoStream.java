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

/**
 * 
 * @author Tobias Just
 *
 */
public class VideoStream extends Thread{
	/**
	 * With this frame rate the video is written to the stream.
	 */
	private static final int FRAME_RATE = 30;
	/**
	 * The size of the text on the video stream.
	 */
	private static final int TEXT_SIZE = 10;
	/**
	 * Store video streams in a HashMap to retrieve them for later usage.
	 */
	private HashMap<Integer, IMediaWriter> streams = new HashMap<Integer, IMediaWriter>();
	/**
	 * Default file name for the recording video.
	 */
	public static String outputFilename = "debug.mp4";
	/**
	 * The writer images will be append to for encoding a video stream.
	 */
	private IMediaWriter writer;
	/**
	 * Start time of the stream.
	 */
	private long startTime;
	/**
	 * Image of the video stream.
	 */
	private BufferedImage image;
	/**
	 * Streamer thread which handles image input from the OBU.
	 */
	private VideoStreamer streamer;
	/**
	 * User ID of the user who currently record the stream.
	 */
	private int userID;
	/**
	 * Recording stream state.
	 */
	private boolean running = false;

	/**
	 * Constructor of the video stream thread to set the user id and vehicle id
	 * as well to know the streamer of the incoming images from an OBU.
	 * 
	 * @param streamer	Streamer thread of an OBU which set incoming images.
	 * @param userID	User ID of the current user.
	 * @param vehicleID	ID of the vehicle the user wants to record.
	 */
	public VideoStream(VideoStreamer streamer, int userID, int vehicleID) {
		this.streamer = streamer;
		this.userID = userID;

		// Retrieve vehicle name and user name from the database
		String vehicleName = AmberServer.getDatabase().getVehicleName(vehicleID);
		String userName = AmberServer.getDatabase().getUserName(userID);

		// Build path and filename
		Path path = Paths.get(Constants.DATA_FOLDER+"//"+userName+"//"+vehicleName);
		if (!Files.exists(path)) (new File(path.toString())).mkdirs();	
		outputFilename = path.toString()+"//"+new SimpleDateFormat("yyyy-MM-dd HH-mm-ss'.mp4'").format(new Date());

		// Make a IMediaWriter to write the file.
		writer = ToolFactory.makeWriter(outputFilename);

		// Tell the writer to add one video stream, with id 0,
		// at position 0, and that it will have a fixed size.
		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 
				320, 240);

		// Set the start time of the writer.
		startTime = System.nanoTime();
		streams.put(0, writer);
	}
	/**
	 * Convert the image to a type the writer could write to a stream. If the image is already
	 * the write type, let it be untouched.
	 * 
	 * @param sourceImage
	 * @param targetType
	 * @return
	 */
	public static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
		BufferedImage image;

		// If the source image is already the target type, return the source image
		if (sourceImage.getType() == targetType)
			image = sourceImage;
		// Otherwise create a new image of the target type and draw the new image
		else {
			image = new BufferedImage(sourceImage.getWidth(), 
					sourceImage.getHeight(), targetType);
			image.getGraphics().drawImage(sourceImage, 0, 0, null);
		}
		return image;
	}
	/**
	 * Start recording the current stream by starting a new thread.
	 */
	public void startRecord() {
		this.running = true;
		start();
	}
	/**
	 * Tell the writer to close and write the video to the file system.
	 * Also store the path to the video file in the database according to the userID.
	 */
	public void getVideoStream(){
		this.running  = false;
		streams.get(0).close();

		// Store the path of the written file in the database for the user
		AmberServer.getDatabase().storeVideostream(userID, outputFilename);

		// Set the video file as new download file 
		VideoDownload.videoFileName = outputFilename;
	}
	/**
	 * Add single images to a stream writer to decode a video.
	 * This method also add telemetry data as text on the video images.
	 * 
	 * @param response
	 */
	public void writeToStream(WebsocketResponse response) {
		// return if there is no data to add to the stream
		if(response == null)
			return;
		// get image from response object
		byte[] imageInByte = response.getImage();
		// get telemetry data from response object
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
			g.setFont(new Font("default", Font.BOLD, TEXT_SIZE));

			//			g.drawString(telemetry.toString(), 0, bgrScreen.getHeight()-20);
			g.drawString("Speed", 10, bgrScreen.getHeight()-(TEXT_SIZE+5));
			g.drawString(""+telemetry.getSpeed(), 60, bgrScreen.getHeight()-(TEXT_SIZE+5));
			g.drawString("Lat|Lon", 10, bgrScreen.getHeight()-(2*(TEXT_SIZE+5)));
			g.drawString(+Math.round(telemetry.getLat()*100.0)/100.0+"|"+Math.round(telemetry.getLon()*100.0)/100.0,
					60, bgrScreen.getHeight()-(2*(TEXT_SIZE+5)));
			g.drawString("KM", 10, bgrScreen.getHeight()-(3*(TEXT_SIZE+5)));
			g.drawString(""+telemetry.getKm(), 60, bgrScreen.getHeight()-(2*(TEXT_SIZE+5)));
			g.drawString("Fuel", 10, bgrScreen.getHeight()-(4*(TEXT_SIZE+5)));
			g.drawString(""+telemetry.getFuel(), 60, bgrScreen.getHeight()-(4*(TEXT_SIZE+5)));
			
			g.drawString("Gyro Z", 120, bgrScreen.getHeight()-(TEXT_SIZE+5));
			g.drawString(""+telemetry.getGyro_z(), 250, bgrScreen.getHeight()-(TEXT_SIZE+5));
			g.drawString("Gyro Y", 120, bgrScreen.getHeight()-(2*(TEXT_SIZE+5)));
			g.drawString(""+telemetry.getGyro_y(), 250, bgrScreen.getHeight()-(2*(TEXT_SIZE+5)));
			g.drawString("Gyro X", 120, bgrScreen.getHeight()-(3*(TEXT_SIZE+5)));
			g.drawString(""+telemetry.getGyro_x(), 250, bgrScreen.getHeight()-(3*(TEXT_SIZE+5)));

			// encode the image to stream #0
			if(running)
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
	/**
	 * 
	 */
	@Override
	public void run() {
		while(running){
			writeToStream(this.streamer.getResponse());
		}
	}
}