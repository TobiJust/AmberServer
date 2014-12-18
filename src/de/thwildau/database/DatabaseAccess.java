package de.thwildau.database;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import de.thwildau.util.ServerLogger;


public class DatabaseAccess {

	protected static final boolean DEBUG = true;
	// JDBC
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;

	/**
	 * Grant access to SQLite Database
	 * @throws Exception
	 */
	public DatabaseAccess() throws Exception{

		// this will load the SQLite driver, each DB has its own driver
		Class.forName("org.sqlite.JDBC");

		// setup the connection with the DB.
		connect = DriverManager.getConnection("jdbc:sqlite:amber.db");

		Runtime.getRuntime().addShutdownHook(new Thread() { 
			public void run() { 
				try { 
					if (!connect.isClosed() && connect != null) { 
						close(null); 
						if (connect.isClosed()) 
							ServerLogger.log("Connection to Database closed", DEBUG); 
					} 
				} catch (SQLException e) { 
					e.printStackTrace(); 
				} 
			} 
		}); 
	}

	/**
	 * Login check with Database.
	 * 
	 * @param username	Username from Login
	 * @param pass	Password from Login as Hash MD5
	 * @return Validation of Login (true if User/Password in Database)
	 */
	public int login(String username, byte[] pass) {
		int user_id = -1;
		String query = "SELECT user_id FROM user WHERE user_name=? AND user_pw=?";
		String query2 = "INSERT INTO session (user_id, time_stamp) values (?, ?)";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.setBytes(2, pass);
			ResultSet rs = preparedStatement.executeQuery();
			if(rs.next()){
				user_id = rs.getInt(1);
				preparedStatement = connect.prepareStatement(query2);
				preparedStatement.setInt(1, user_id);
				preparedStatement.setLong(2, System.currentTimeMillis());
				preparedStatement.executeUpdate();
			}
			else
				user_id = -1;
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return user_id;
	}
	/**
	 * Update the session when a User logs in again.
	 * @param user_id	Current User
	 */
	public void updateSession(int user_id){
		String query = "UPDATE session SET time_stamp=? WHERE user_id=?";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setLong(1, System.currentTimeMillis());
			preparedStatement.setInt(2, user_id);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	/**
	 * Logout a User, remove from Session Table.
	 * @param user_id	Current User
	 */
	public void logout(int user_id) {
		String query = "DELETE FROM session WHERE user_id=?";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, user_id);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Check, if the current User is still online.
	 * @param user_id	Current User
	 * @return	True, if he's online
	 * 			False, if not.
	 */
	public boolean checkOnline(int user_id){
		boolean isOnline = false;
		String query = "SELECT COUNT(*) FROM session WHERE user_id=?";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, user_id);
			ResultSet rs = preparedStatement.executeQuery();
			if(rs.getInt(1) > 0){
				isOnline = true;
				updateSession(user_id);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isOnline;
	}
	/**
	 * Add a registration id for mobile Devices.
	 * 
	 * @param username Username from Login
	 * @param regid	GCM Registration ID
	 * @return validation of GCM Registration
	 */
	public void registerGCM(int user_id, String regid) {
		//		String query = "insert into GCM (user_id, gcm_regid) values (?, ?)";
		String query = "INSERT INTO GCM (user_id, gcm_regid) SELECT ?, ?"
				+ "WHERE NOT EXISTS (SELECT 1 FROM GCM WHERE user_id = ? and gcm_regid = ?);";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, user_id);
			preparedStatement.setString(2, regid);
			preparedStatement.setInt(3, user_id);
			preparedStatement.setString(4, regid);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Delete a registration id for mobile Devices.
	 * 
	 * @param username Username from Login
	 * @param regid	GCM Registration ID
	 * @return validation of GCM Unregistration
	 */
	public boolean unregisterGCM(String username) {
		boolean unregister = false;
		String query = "DELETE FROM gcm WHERE user_name=?";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setString(1, username);
			ResultSet rs = preparedStatement.executeQuery();
			if(rs.next())
				unregister = true;
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return unregister;
	}

	/**
	 * Add a new User to the Database.
	 * 
	 * @param username from Register
	 * @param pass	Password from Register as Hash MD5
	 * @return Successful/Failed entry in database
	 */
	public boolean addUser(String username, byte[] pass){
		int rows = 0;
		String query = "insert into USER(user_name, user_pw) values (?, ?)";
		try {
			preparedStatement = connect.prepareStatement(query);
			// "user_name, user_pw);
			// parameters start with 1
			preparedStatement.setString(1, username);
			preparedStatement.setBytes(2, pass);
			rows = preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return (rows > 0) ? true : false;
	}
	/**
	 * Add a new Vehicle to the User
	 * 
	 * @return Success/Error
	 */
	public boolean registerVehicle(int user_id, String vehicle_id){
		int rows = 0;
		String query = "Insert into VehiclePerUser(user_id, vehicle_id) values (?, ?)";
		if(getVehicleName(vehicle_id) == null)
			return false;
		try {
			preparedStatement = connect.prepareStatement(query);
			// "user_name, user_pw);
			// parameters start with 1
			preparedStatement.setInt(1, user_id);
			preparedStatement.setString(2, vehicle_id);
			rows = preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return (rows > 0) ? true : false;
	}
	public boolean unregisterVehicle(int user_id, String vehicle_id){
		boolean unregister = false;
		String query = "DELETE FROM VehiclePerUser WHERE user_id=? AND vehicle_id=?";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, user_id);
			preparedStatement.setString(2, vehicle_id);
			ResultSet rs = preparedStatement.executeQuery();
			if(rs.next())
				unregister = true;
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return unregister;
	}

	private String getVehicleName(String vehicle_id){
		String query = "Select vehicle_name from Vehicle where vehicle_id=?";
		String name = null;
		try{
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setString(1, vehicle_id);
			ResultSet rs2 = preparedStatement.executeQuery();
			while(rs2.next())
				name = rs2.getString(1);
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return name;
	}

	/**
	 * Execute Query to show all current User.
	 * 
	 * @return List of all User.
	 */
	public String showAllUser(){
		String result = "\n";
		try {
			statement = connect.createStatement();
			ResultSet rs = statement.executeQuery( "SELECT * FROM USER;" );
			result += writeResultSet(rs);
			close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Get GCM Registration Ids.
	 * 
	 * @return List of all Registration Ids.
	 */
	public List<String> getGCMRegIds(){
		List<String> regIds = new ArrayList<String>();
		try {
			statement = connect.createStatement();
			ResultSet rs = statement.executeQuery( "SELECT gcm_regid FROM GCM;" );
			ResultSetMetaData rm = rs.getMetaData();
			String colName = rm.getColumnName(1);

			while (rs.next())
				regIds.add(rs.getString(colName));

			//			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return regIds;
	}
	/**
	 * Return all Vehicles mapped to the User.
	 * @param userID	Current User
	 * @return	List of Vehicle Objects
	 */
	public ArrayList<String> getVehicles(int userID){
		ArrayList<String> vehicle_ids = new ArrayList<String>();
		String query = "SELECT Vehicle_id FROM VehiclePerUser WHERE user_id=?";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, userID);
			ResultSet rs = preparedStatement.executeQuery();
			while(rs.next())
				vehicle_ids.add(rs.getString(1));
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return vehicle_ids;
	}
	/**
	 * Return all Events mapped to a Vehicle Object.
	 * @param vehicleID	Current Vehicle Object
	 * @return	List of Event Objects
	 */
	public Object[] getEvents(String vehicleID){
		Object[] data = new Object[2];
		ArrayList<Integer> event_ids = new ArrayList<Integer>();
		String name = null;
		String query1 = "SELECT Event_id FROM EventPerVehicle WHERE vehicle_id=?";
		String query2 = "SELECT Vehicle_Name FROM Vehicle WHERE vehicle_id=?";
		try {
			preparedStatement = connect.prepareStatement(query1);
			preparedStatement.setString(1, vehicleID);
			ResultSet rs1 = preparedStatement.executeQuery();
			while(rs1.next())
				event_ids.add(rs1.getInt(1));
			preparedStatement.close();

			preparedStatement = connect.prepareStatement(query2);
			preparedStatement.setString(1, vehicleID);
			ResultSet rs2 = preparedStatement.executeQuery();
			while(rs2.next())
				name = rs2.getString(1);
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		data[0] = name;
		data[1] = event_ids;
		return data;
	}

	public boolean storeVideostream(int userID, String pathToVideo){
		int rows = 0;
		String query = "INSERT INTO video (user_id, video_path, time_stamp) values (?, ?, ?)";
		try {
			preparedStatement = connect.prepareStatement(query);
			// "user_name, user_pw);
			// parameters start with 1
			preparedStatement.setInt(1, userID);
			preparedStatement.setString(2, pathToVideo);
			rows = preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return (rows > 0) ? true : false;
		
	}
	/**
	 * Return the data of an Event.
	 * @param eventID	Current Event
	 * @return	All Attributes of an Event in an Array.
	 */
	public Object[] getEventData(int eventID){
		Object[] eventData = null;
		String query = "SELECT * FROM Event WHERE event_id=?";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, eventID);
			ResultSet rs = preparedStatement.executeQuery();

			ResultSetMetaData rm = rs.getMetaData();
			eventData = new Object[rm.getColumnCount()];
			while(rs.next()){
				int i;
				for(i = 0; i < eventData.length-1; i++)
					eventData[i] = rs.getObject(i+1);
				try {
					BufferedImage image = ImageIO.read(rs.getBinaryStream(eventData.length));
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(image, "jpg", baos);
					baos.flush();
					byte[] imageInByte = baos.toByteArray();
					baos.close();
					eventData[i] = imageInByte;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return eventData;
	}

	/**
	 * Prints a readable Output.
	 * 
	 * @param resultSet
	 * @return String output of a Result Set.
	 * @throws SQLException
	 */
	private String writeResultSet(ResultSet resultSet) throws SQLException {
		String result = "";
		ResultSetMetaData rm = resultSet.getMetaData();
		String colNames[] = new String[rm.getColumnCount()];

		for (int ctr = 1; ctr <= colNames.length; ctr++) {
			String s = rm.getColumnName(ctr);
			colNames[ctr - 1] = s;
		}

		while (resultSet.next()){
			for(String row : colNames)
				result += resultSet.getString(row) + "\t";
			result += "\n";
		}
		return result;
	}

	// you need to close all three to make sure
	public void close(ResultSet resultSet) {
		try {
			if(resultSet != null) resultSet.close();
			if(statement != null) statement.close();
			connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
