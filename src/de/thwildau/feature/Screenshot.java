package de.thwildau.feature;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import de.thwildau.model.Telemetry;
import de.thwildau.webserver.WebsocketResponse;

/**
 * 
 * @author Tobias Just
 *
 */
@WebServlet("/screenshot")
public class Screenshot extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static String screenShotFileName = "responseImage.jpg";
	public static WebsocketResponse websocketResponse;
	public static Telemetry telemetry;
	public static byte[] image;
	public static double lat;
	public static double lon;

	/**
	 * 
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);

	}
	/**
	 * 
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		//		File file = new File("responseImage.jpg");
		//		FileInputStream fin = null;
		//		// create FileInputStream object
		//		fin = new FileInputStream(file);
		//		byte fileContent[] = new byte[(int)file.length()];
		//		// Reads up to certain bytes of data from this input stream into an array of bytes.
		//		fin.read(fileContent);

		//		image = fileContent;
		if(websocketResponse != null){
			image = websocketResponse.getImage();
			telemetry = (Telemetry)websocketResponse.getData();
			if(image != null){
				InputStream in = new ByteArrayInputStream(image);
				BufferedImage bufImage = ImageIO.read(in);
				// get the graphics for the image
				Graphics2D g = bufImage.createGraphics();

				// add text to the picture
				g.setColor(Color.WHITE);
				g.setFont(new Font("default", Font.BOLD, 15));
				g.drawString("Lat|Lon", 10, bufImage.getHeight()-20);
				if(telemetry != null){
					System.out.println(telemetry);		
					g.drawString(Math.round(telemetry.getLat()*100.0)/100.0+"|"+Math.round(telemetry.getLon()*100.0)/100.0,
							80, bufImage.getHeight()-(20));
				}
				g.drawString("Time ", 10, bufImage.getHeight()-45);
				g.drawString(new SimpleDateFormat("dd.MM.yyy HH:mm:ss").format(new Date()), 80, bufImage.getHeight()-45);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write( bufImage, "jpg", baos );
				baos.flush();
				image = baos.toByteArray();
				baos.close();
			}

			response.setContentType("text/html");
			// Prints the image to the website
			PrintWriter out = response.getWriter();
			out.println("<img src='data:image/png;base64," + DatatypeConverter.printBase64Binary(image) + "'>");
		}
		else{
			response.setContentType("text/html");
			// Prints the image to the website
			PrintWriter out = response.getWriter();
			out.println("<h1> No Current Image </h1>");
		}
	}
}
