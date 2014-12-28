package xuggler;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.websocket.Session;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

import de.thwildau.util.ServerLogger;

public class DesktopStream extends Thread{

	private static final double FRAME_RATE = 25;

	private static final int SECONDS_TO_RUN_FOR = 300;

	//	private static final String outputFilename = "mydesktop_"+(int)FRAME_RATE+"fps"+SECONDS_TO_RUN_FOR+"s.mp4";

	private static final boolean DEBUG = true;

	private static Dimension screenBounds;

	private Session session;
	private String vehicleID;

	private boolean running;

	public DesktopStream(Session session, String vehicleID){
		this.session = session;
		this.vehicleID = vehicleID;		
	}

	public void startStream(){	
		if(!this.running){
			this.running = true;
			start();
		}
	}
	public void run(){
		ServerLogger.log("Stream started for Session " + session.getId(), DEBUG);
		// let's make a IMediaWriter to write the file.
		//		final IMediaWriter writer = ToolFactory.makeWriter(outputFilename);

		screenBounds = Toolkit.getDefaultToolkit().getScreenSize();

		// We tell it we're going to add one video stream, with id 0,
		// at position 0, and that it will have a fixed frame rate of FRAME_RATE.
		//		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 
		//				screenBounds.width/2, screenBounds.height/2);

		long startTime = System.nanoTime();

		while(running) {

			// take the screen shot
			BufferedImage screen = getDesktopScreenshot();

			// convert to the right image type
			BufferedImage bgrScreen = convertToType(screen, 
					BufferedImage.TYPE_3BYTE_BGR);

			// encode the image to stream #0
			//			writer.encodeVideo(0, bgrScreen, System.nanoTime() - startTime, 
			//					TimeUnit.NANOSECONDS);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ImageIO.write( bgrScreen, "jpg", baos );
				baos.flush();
				byte[] imageInByte = baos.toByteArray();
				baos.close();
				ByteBuffer buf = ByteBuffer.wrap(imageInByte);
				session.getBasicRemote().sendBinary(buf);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// sleep for frame rate milliseconds
			try {
				Thread.sleep((long) (1000 / FRAME_RATE));
			} 
			catch (InterruptedException e) {
				// ignore
			}

		}

		// tell the writer to close and write the trailer if  needed
		//		writer.close();
	}

	public void stopStream(){
		this.running = false;
		ServerLogger.log("Stream finished for Session " + session.getId(), DEBUG);

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

	private static BufferedImage getDesktopScreenshot() {
		try {
			Robot robot = new Robot();
			Rectangle captureSize = new Rectangle(screenBounds);
			return robot.createScreenCapture(captureSize);
		} 
		catch (AWTException e) {
			e.printStackTrace();
			return null;
		}

	}

}
