package de.thwildau.model;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * 
 * @author Tobias Just
 *
 */
public class ImageData {

	private ByteArrayOutputStream image = new ByteArrayOutputStream();
	private byte[] byteArray;

	public ImageData(){
	}
	/**
	 * 
	 * @param list
	 */
	public void addData(List<Byte> list){
		try {
			byte[] bytes = new byte[list.size()];
			for(int i=0; i < list.size(); i++)
				bytes[i] = list.get(i);
			byteArray = bytes;
//			image.write(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @return
	 */
	public byte[] getData() {
//		if(image.size() > 0)
//			return image.toByteArray();
		if(byteArray.length > 1)
			return byteArray;
		return null;
	}
}
