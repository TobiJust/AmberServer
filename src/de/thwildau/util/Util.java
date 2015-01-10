package de.thwildau.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.thwildau.server.AmberServer;

/**
 * 
 * @author Tobias Just
 *
 */
public class Util {

	/**
	 * Add a user manually from the command line.
	 * @throws IOException
	 */
	public static void addUser() throws IOException{
		BufferedReader din = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Insert username: ");
		String username = din.readLine();
		System.out.print("Insert passwort: ");
		byte[] password = Util.passwordToHash(din.readLine());
		System.out.println("Should this user be admin? (1 - true, 0 - false");
		int isAdmin = Integer.parseInt(din.readLine());
		boolean added = AmberServer.getDatabase().addUser(username, password, isAdmin);
		if(added)
			ServerLogger.log(Constants.SUCCESS_REGISTER, Constants.DEBUG);
		else
			ServerLogger.log(Constants.ERROR_REGISTER, Constants.DEBUG);
			
	}
	/**
	 * 
	 * @throws IOException
	 */
	public static void addVehicle() throws IOException{
		BufferedReader din = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Insert vehicle name: ");
		String vehcilename = din.readLine();
		System.out.print("Choose logo for new Vehicle: ");
		byte[] image = openFilechooser();
		boolean added = AmberServer.getDatabase().addVehicle(vehcilename, image);
		if(added)
			ServerLogger.log(Constants.SUCCESS_ADD_VEHICLE, Constants.DEBUG);
		else
			ServerLogger.log(Constants.ERROR_ADD_VEHICLE, Constants.DEBUG);
			
	}
	/**
	 * 
	 * 
	 * @param l
	 * @return
	 */
	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}
	
	/**
	 * 
	 * @param pass
	 * @return
	 */
	public static byte[] passwordToHash(String pass){
		byte[] hashed = null;
		try {
			// Create MessageDigest instance for MD5
			MessageDigest md = MessageDigest.getInstance("MD5");
			//Add password bytes to digest
			md.update(pass.getBytes());
			//Get the hash's bytes 
			hashed = md.digest();            
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		return hashed;
	}
	
	private static byte[] openFilechooser() { 
		byte[] imageData = null;
        final JFileChooser chooser = new JFileChooser("Verzeichnis wählen"); 
        chooser.setDialogType(JFileChooser.OPEN_DIALOG); 
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); 
        FileFilter imageFilter = new FileNameExtensionFilter(
        	    "Image files", ImageIO.getReaderFileSuffixes());
        chooser.setFileFilter(imageFilter);
        final File directory = new File("~"); 

        chooser.setCurrentDirectory(directory); 

        chooser.addPropertyChangeListener(new PropertyChangeListener() { 
            public void propertyChange(PropertyChangeEvent e) { 
                if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY) 
                        || e.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) { 
                    final File f = (File) e.getNewValue(); 
                } 
            } 
        }); 

        chooser.setVisible(true); 
        final int result = chooser.showOpenDialog(null); 

		if (result == JFileChooser.APPROVE_OPTION) { 
            File file = chooser.getSelectedFile(); 
            String pathToFile = file.getPath(); 
            System.out.println(pathToFile); 
            Path path = Paths.get(pathToFile);
			try {
				imageData = Files.readAllBytes(path);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        } 
        
        chooser.setVisible(false); 
        return imageData;
    } 
	
}
