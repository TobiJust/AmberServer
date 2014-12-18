package de.thwildau.stream;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

public class VideoStream {

	private static final double FRAME_RATE = 50;
	private static final int SECONDS_TO_RUN_FOR = 10;

	private HashMap<Integer, IMediaWriter> streams = new HashMap<Integer, IMediaWriter>();

	private static final String outputFilename = "mydesktop_"+(int)FRAME_RATE+"fps"+SECONDS_TO_RUN_FOR+"s.mp4";

	private static Dimension screenBounds;
	private IMediaWriter writer;

	public VideoStream(int userID) {
		// let's make a IMediaWriter to write the file.
		writer = ToolFactory.makeWriter(outputFilename);

		screenBounds = Toolkit.getDefaultToolkit().getScreenSize();

		// We tell it we're going to add one video stream, with id 0,
		// at position 0, and that it will have a fixed frame rate of FRAME_RATE.
		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 
				screenBounds.width/2, screenBounds.height/2);

		streams.put(userID, writer);
	}
	public void writeToStream(BufferedImage image){

		long startTime = System.nanoTime();


		// convert to the right image type
		BufferedImage bgrScreen = convertToType(image, 
				BufferedImage.TYPE_3BYTE_BGR);

		// encode the image to stream #0
		writer.encodeVideo(0, bgrScreen, System.nanoTime() - startTime, 
				TimeUnit.NANOSECONDS);

		// sleep for frame rate milliseconds
		try {
			Thread.sleep((long) (1000 / FRAME_RATE));
		} 
		catch (InterruptedException e) {
			// ignore
		}

		// tell the writer to close and write the trailer if  needed
		//		writer.close();

	}
	//	public void startRecording(int userID){
	//		IMediaWriter streamWriter = streams.get(userID);
	//		streamWriter.encodeVideo(0, img, System.nanoTime()-, arg3);
	//	}

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
	
	public void getVideoStream(){
		writer.close();
	}

}