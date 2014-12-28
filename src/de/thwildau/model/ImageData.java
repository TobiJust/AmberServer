package de.thwildau.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageData {

	private ByteArrayOutputStream image = new ByteArrayOutputStream();

	public ImageData(){

	}

	public void addData(List<Byte> list){
		try {
			byte[] bytes = new byte[list.size()];
			for(int i=0; i < list.size(); i++)
				bytes[i] = list.get(i);
			image.write(bytes);
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
			System.out.println("Write To File 2");
			// convert byte array back to BufferedImage
			InputStream in = new ByteArrayInputStream(image.toByteArray());
			BufferedImage bImageFromConvert = ImageIO.read(in);

			ImageIO.write(bImageFromConvert, "jpg", new File(
					"responseImage1.jpg"));

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
