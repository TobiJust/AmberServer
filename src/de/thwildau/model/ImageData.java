package de.thwildau.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageData {

	private ByteArrayOutputStream image = new ByteArrayOutputStream();

	public ImageData(){

	}

	public void addData(byte[] data){
		try {
			image.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getData() {
		return image.toByteArray();
	}
	public BufferedImage getBufferedImage(){
		// convert byte array back to BufferedImage
		InputStream in = new ByteArrayInputStream(image.toByteArray());
		try {
			return ImageIO.read(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void writeImageToFile(){
		try {

			// convert byte array back to BufferedImage
			InputStream in = new ByteArrayInputStream(image.toByteArray());
			BufferedImage bImageFromConvert = ImageIO.read(in);

			ImageIO.write(bImageFromConvert, "jpg", new File(
					"responseImage.jpg"));

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
