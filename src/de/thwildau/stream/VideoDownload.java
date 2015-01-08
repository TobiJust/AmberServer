package de.thwildau.stream;


import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
	private String videoFileName;


	public VideoDownload(String videoFile) {
		super();
		this.videoFileName = videoFile;
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		System.out.println(this.videoFileName);
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition",
				"attachment;filename=video.mp4");

		ServletOutputStream out = response.getOutputStream();

		Path path = Paths.get(videoFileName);
		try{
			byte[] data = Files.readAllBytes(path);
			out.write(data);
			out.flush();
			out.close();
		}catch(Exception e){
			System.err.println("No File - " + videoFileName);
		}
	}

	/**
	 * Returns the output from the given URL.
	 * 
	 * I tried to hide some of the ugliness of the exception-handling
	 * in this method, and just return a high level Exception from here.
	 * Modify this behavior as desired.
	 * 
	 * @param desiredUrl
	 * @throws Exception
	 */
	public void doHttpUrlConnectionAction()
			throws Exception
	{
		URL url = null;
		BufferedReader reader = null;

		try
		{
			// create the HttpURLConnection
			url = new URL("http://localhost:3001/send");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// just want to do an HTTP GET here
			connection.setRequestMethod("GET");

			// uncomment this if you want to write output to this url
			//connection.setDoOutput(true);

			// give it 15 seconds to respond
			connection.setReadTimeout(15*1000);
			connection.connect();

			// read the output from the server
			connection.getInputStream();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			// close the reader; this can throw an exception too, so
			// wrap it in another try/catch block.
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		}
	}

}
