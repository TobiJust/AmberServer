package de.thwildau.stream;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Tobias Just
 *
 */
@WebServlet("/download")
public class VideoDownload extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static String videoFileName;

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

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition",
				"attachment;filename="+videoFileName);

		ServletOutputStream out = response.getOutputStream();

		try{
			Path path = Paths.get(videoFileName);
			byte[] data = Files.readAllBytes(path);
			out.write(data);
			out.flush();
			out.close();
		}catch(Exception e){
			System.err.println("No File - " + videoFileName);
		}
	}
}
