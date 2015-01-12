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

import de.thwildau.model.Vehicle;
import de.thwildau.util.Constants;
import de.thwildau.util.ServerLogger;


public class DatabaseManager {
	/**
	 *  JDBC Connection to the SQLite Database
	 */
	private Connection connect = null;
	/**
	 * SQLite Statement to execute queries.
	 */
	private Statement statement = null;
	/**
	 * SQLite Statements that will be prepared for later queries. 
	 * They contain placeholder to fill up with variables.
	 */
	private PreparedStatement preparedStatement = null;
	/**
	 * Grant access to SQLite Database and manage all requests to and responses 
	 * from the database.
	 * 
	 * @throws Exception	Throw Exception if the database is locked or a sql 
	 * 						statement cant' be executed.
	 */
	public DatabaseManager() throws Exception{

		// this will load the SQLite driver, each DB has its own driver
		Class.forName("org.sqlite.JDBC");

		// setup the connection with the DB
		connect = DriverManager.getConnection("jdbc:sqlite:amber.db");

		// Run the database connection in a seperate thread
		Runtime.getRuntime().addShutdownHook(new Thread() { 
			public void run() { 
				try { 
					if (!connect.isClosed() && connect != null) { 
						close(null); 
						if (connect.isClosed()) 
							ServerLogger.log("Connection to Database closed", Constants.DEBUG); 
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
	 * @return User ID from database to handle future requests.
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
	 * Login Check from Web App. It looks for correct user name and password as well as if the user
	 * is authorized with an admin flag.
	 * If is_admin:
	 * 		-1 - Wrong Username or Password
	 * 		 0 - User not allowed
	 * 		 1 - Access granted for User
	 * 
	 * @param username Username from Web App Login
	 * @param pass Password from Web App Login as Hash MD5
	 * @return User ID from database to handle future requests.
	 */
	public int adminLogin(String username, byte[] pass) {
		int user_id = -1;
		int is_admin = -1;
		String query = "SELECT user_id, is_admin FROM user WHERE user_name=? AND user_pw=?";
		String query2 = "INSERT INTO session (user_id, time_stamp) values (?, ?)";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.setBytes(2, pass);
			ResultSet rs = preparedStatement.executeQuery();
			if(rs.next()){
				user_id = rs.getInt(1);
				is_admin = rs.getInt(2);
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
		if(is_admin == 0)
			user_id = -2;
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
	public boolean logout(int user_id) {
		int rows = 0;
		String query = "DELETE FROM session WHERE user_id=?";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, user_id);
			rows = preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return (rows > 0) ? true : false;
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
	 * @param username 	The username from register dialog.
	 * @param pass	Password from register dialog as Hash MD5.
	 * @return Successful/Failed entry in database.
	 */
	public boolean addUser(String username, byte[] pass, int isAdmin){
		int rows = 0;
		String query = "insert into USER(user_name, user_pw, is_admin) values (?, ?, ?)";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.setBytes(2, pass);
			preparedStatement.setInt(3, isAdmin);
			rows = preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return (rows > 0) ? true : false;
	}
	/**
	 * Add a new Vehicle to the Database.
	 * 
	 * @param vehiclename	The name of the new vehicle, that should be registered.
	 * @param imageInBytes	A logo of the vehicle, which will show up by car picking in app and web app.
	 * @return	Successful/Failed entry in database.
	 */
	public boolean addVehicle(String vehiclename, byte[] imageInBytes){
		int rows = 0;
		String query = "insert into VEHICLE(vehicle_name, vehicle_logo) values (?, ?)";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setString(1, vehiclename);
			preparedStatement.setBytes(2, imageInBytes);
			rows = preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return (rows > 0) ? true : false;
	}
	/**
	 * Register a new Vehicle to the User.
	 * 
	 * @param user_id	ID of the user, the vehicle should mapped to.
	 * @param vehicle_id	ID of the vehicle to register.
	 * @return	The vehicle mapped to the user.
	 */
	public Vehicle registerVehicle(int user_id, int vehicle_id){
		int rows = 0;
		Vehicle vehicle = null;
		String query = "Insert into VehiclePerUser(user_id, vehicle_id, alarm_status, add_date) values (?, ?, ?, ?)";
		if(getVehicleName(vehicle_id) == null)
			return null;
		try {
			long date = System.currentTimeMillis();
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, user_id);
			preparedStatement.setInt(2, vehicle_id);
			preparedStatement.setInt(3, 0);
			preparedStatement.setLong(4, date);
			rows = preparedStatement.executeUpdate();
			if(rows > 0){
				vehicle = new Vehicle();
				vehicle.setDate(""+date);
				vehicle.setAlarmStatus(0);
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vehicle;
	}
	/**
	 * Unregister a vehicle for the user if he removes it from his list.
	 * 
	 * @param user_id	ID of the user who wants to remove a vehicle.
	 * @param vehicle_id	ID of the vehicle which should be removed.
	 * @return
	 */
	public boolean unregisterVehicle(int user_id, int vehicle_id){
		int rows = 0;
		String query = "DELETE FROM VehiclePerUser WHERE user_id=? AND vehicle_id=?";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, user_id);
			preparedStatement.setInt(2, vehicle_id);
			rows = preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return (rows > 0) ? true : false;
	}
	/**
	 * Get the vehicle name for the given ID.
	 * 
	 * @param vehicle_id	ID of the vehicle to know the name about.
	 * @return	Vehicle name as String.
	 */
	public String getVehicleName(int vehicle_id){
		String query = "Select vehicle_name from Vehicle where vehicle_id=?";
		String name = null;
		try{
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, vehicle_id);
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
	 * Get the user name for the given ID.
	 * 
	 * @param user_id	ID of the name to know the name about.
	 * @return	User name as String.
	 */
	public String getUserName(int user_id){
		String query = "Select user_name from User where user_id=?";
		String name = null;
		try{
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, user_id);
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
	 * Toggle the alarm for the notificatin status. It set the alarm status for the user 
	 * and favorite vehicle. 
	 * 
	 * @param user_id	ID of the user who wants to set the alarm.
	 * @param vehicle_id	ID of the vehicle the user wants to be alarmed by events.
	 * @param status	Status to be set.
	 * @return	The set status.
	 */
	public boolean toggleAlarm(int user_id, int vehicle_id, boolean status){
		int statusAsInt = status ? 1 : 0;
		String query = "UPDATE vehiclePerUser SET alarm_status=? WHERE user_id=? AND vehicle_id=?";
		int rows = 0;
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, statusAsInt);
			preparedStatement.setInt(2, user_id);
			preparedStatement.setInt(3, vehicle_id);
			rows  = preparedStatement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} 
		if(rows > 0)
			return status;
		else
			return !status;
	}
	/**
	 * Execute Query to show all current User.
	 * 
	 * @return List of all User as String.
	 */
	public String showAllUser(){
		String result = "\n";
		try {
			statement = connect.createStatement();
			ResultSet rs = statement.executeQuery( "SELECT * FROM USER;" );
			result += writeResultSet(rs);
			//			close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Execute Query to show all current Vehicles.
	 * 
	 * @return List of all Vehicles as String.
	 */
	public String showAllVehicles(){
		String result = "\n";
		try {
			statement = connect.createStatement();
			ResultSet rs = statement.executeQuery( "SELECT * FROM VEHICLE;" );
			result += writeResultSet(rs);
			//			close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Get GCM Registration IDs.
	 * 
	 * @return List of all Registration IDs.
	 */
	public List<String> getGCMRegIds(int user_id){
		String query = "SELECT gcm_regid FROM GCM WHERE user_id=?;"; 
		List<String> regIds = new ArrayList<String>();
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, user_id);
			ResultSet rs = preparedStatement.executeQuery();
			ResultSetMetaData rm = rs.getMetaData();
			String colName = rm.getColumnName(1);
			while (rs.next())
				regIds.add(rs.getString(colName));
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
	public ArrayList<Vehicle> getVehicles(int userID){
		ArrayList<Vehicle> vehicleList = new ArrayList<Vehicle>();
		String query = "SELECT Vehicle_id, Alarm_Status, Add_Date FROM VehiclePerUser WHERE user_id=?";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, userID);
			ResultSet rs = preparedStatement.executeQuery();
			while(rs.next()){
				Vehicle vehicle = new Vehicle();
				vehicle.setVehicleID(rs.getInt(1));
				vehicle.setAlarmStatus(rs.getInt(2));
				vehicle.setDate(rs.getString(3));
				vehicleList.add(vehicle);
			}
			//				vehicle_ids.put(rs.getString(1), rs.getInt(2));
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return vehicleList;
	}


	public int addEvent(int vehicleID, String type, String time, double lat, double lon, byte[] image){
		int eventID = -1;
		String query = "INSERT INTO event (event_type, event_time, event_lat, event_lon, event_image) "
				+ "values (?, ?, ?, ?, ?)";
		String query2 = "INSERT INTO eventPerVehicle (vehicle_id, event_id) values (?,?)";
		String query3 = "select last_insert_rowid();";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setString(1, type);
			preparedStatement.setString(2, time);
			preparedStatement.setDouble(3, lat);
			preparedStatement.setDouble(4, lon);
			preparedStatement.setBytes(5, image);
			int rows = preparedStatement.executeUpdate();
			if(rows > 0){
				ResultSet rs = connect.prepareStatement(query3).executeQuery();
				while (rs.next())
					eventID = rs.getInt(1);
				if(eventID >= 0){
					preparedStatement = connect.prepareStatement(query2);
					preparedStatement.setInt(1, vehicleID);
					preparedStatement.setInt(2, eventID);
					preparedStatement.executeUpdate();
				}
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return eventID;
	}

	/**
	 * Return all Events mapped to a vehicle ID.
	 * 
	 * @param vehicleID	Current Vehicle Object
	 * @return	List of Event Objects
	 */
	public Object[] getEvents(int vehicleID){
		Object[] data = new Object[3];
		ArrayList<Integer> event_ids = new ArrayList<Integer>();
		String name = null;
		byte[] imageInByte = null;
		String query1 = "SELECT Event_id FROM EventPerVehicle WHERE vehicle_id=?";
		String query2 = "SELECT Vehicle_Name, Vehicle_Logo FROM Vehicle WHERE vehicle_id=?";
		try {
			preparedStatement = connect.prepareStatement(query1);
			preparedStatement.setInt(1, vehicleID);
			ResultSet rs1 = preparedStatement.executeQuery();
			while(rs1.next())
				event_ids.add(rs1.getInt(1));
			preparedStatement.close();

			preparedStatement = connect.prepareStatement(query2);
			preparedStatement.setInt(1, vehicleID);
			ResultSet rs2 = preparedStatement.executeQuery();

			while(rs2.next()){
				name = rs2.getString(1);
				try {
					BufferedImage image = ImageIO.read(rs2.getBinaryStream(2));
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(image, "jpg", baos);
					baos.flush();
					imageInByte = baos.toByteArray();
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		data[0] = name;
		data[1] = event_ids;
		data[2] = imageInByte;
		return data;
	}
	/**
	 * Return all user id that are allowed to get a notification.
	 * 
	 * @param obuID	ID of the vehicle for alarm issues
	 * @return	List of all user id's that will receive a notification
	 */
	public ArrayList<Integer> getNotificationUsers(int obuID) {
		ArrayList<Integer> user = new ArrayList<Integer>();
		String query = "SELECT user_id FROM VehiclePerUser WHERE vehicle_id=? AND alarm_status=1";
		String query2 = "SELECT COUNT(*) FROM session WHERE user_id=?";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, obuID);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()){
				int userID = rs.getInt(1);
				preparedStatement = connect.prepareStatement(query2);
				preparedStatement.setInt(1, userID);
				ResultSet rs2 = preparedStatement.executeQuery();
				if(rs2.getInt(1) != 0)
					user.add(userID);
			}
			//			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return user;
	}
	/**
	 * Store the path of a recorded video in the database by the user.
	 * 
	 * @param userID	ID of the user that wants to store the video.
	 * @param pathToVideo	Path to the video located on the file system.
	 * @return
	 */
	public boolean storeVideostream(int userID, String pathToVideo){
		int rows = 0;
		String query = "INSERT INTO video (user_id, video_path, time_stamp) values (?, ?, ?)";
		try {
			preparedStatement = connect.prepareStatement(query);
			// "user_name, user_pw);
			// parameters start with 1
			preparedStatement.setInt(1, userID);
			preparedStatement.setString(2, pathToVideo);
			preparedStatement.setLong(3, System.currentTimeMillis());
			rows = preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return (rows > 0) ? true : false;

	}
	/**
	 * Return the data of an Event.
	 * 
	 * @param eventID	Current Event ID.
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
					if(image != null){
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ImageIO.write(image, "jpg", baos);
						baos.flush();
						byte[] imageInByte = baos.toByteArray();
						baos.close();
						eventData[i] = imageInByte;
					}					
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

	/**
	 *  Close all open statements, the result set and the database connection.
	 *
	 * @param resultSet
	 */
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
