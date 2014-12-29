package de.thwildau.gcm;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import de.thwildau.util.Constants;
import de.thwildau.util.ServerPreferences;

public class SendNotification {

	public SendNotification(String type, String message, int userID)
	{
		try
		{
			String myUrl = "http://"+InetAddress.getLocalHost().getHostAddress()+":"+
					ServerPreferences.getProperty(Constants.WEB_PORT) +"/" +
					Constants.ARG_GCM_SEND + "?" +
					Constants.ARG_MESSAGE + "=" + message + "&" +
					Constants.ARG_USERID + "="  + userID  + "&" + 
					Constants.ARG_TYPE + "="  + type;
			System.out.println(myUrl);
			// if your url can contain weird characters you will want to 
			// encode it here, something like this:
			// myUrl = URLEncoder.encode(myUrl, "UTF-8");

			doHttpUrlConnectionAction(myUrl);
		}
		catch (Exception e)
		{
			// deal with the exception in your "controller"
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
	private void doHttpUrlConnectionAction(String desiredUrl)
			throws Exception
	{
		URL url = null;
		BufferedReader reader = null;
		StringBuilder stringBuilder;

		try
		{
			// create the HttpURLConnection
			url = new URL(desiredUrl);
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

