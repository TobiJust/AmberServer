package de.thwildau.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class DatabaseAccess {

	// JDBC
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

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
						close(); 
						if (connect.isClosed()) 
							System.out.println("Connection to Database closed"); 
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
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.setBytes(2, pass);
			ResultSet rs = preparedStatement.executeQuery();
			if(rs.next())
				user_id = rs.getInt(1);
			else
				user_id = -1;
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return user_id;
	}

	/**
	 * Add a registration id for mobile Devices.
	 * 
	 * @param username Username from Login
	 * @param regid	GCM Registration ID
	 * @return validation of GCM Registration
	 */
	public boolean registerGCM(int user_id, String regid) {
		int rows = 0;
//		String query = "insert into GCM (user_id, gcm_regid) values (?, ?)";
		String query = "INSERT INTO GCM (user_id, gcm_regid) SELECT ?, ?"
				+ "WHERE NOT EXISTS (SELECT 1 FROM GCM WHERE user_id = ? and gcm_regid = ?);";
		try {
			preparedStatement = connect.prepareStatement(query);
			preparedStatement.setInt(1, user_id);
			preparedStatement.setString(2, regid);
			preparedStatement.setInt(3, user_id);
			preparedStatement.setString(4, regid);
			rows = preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return (rows > 0) ? true : false;
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
		String query = "insert into USER (user_name, user_pw)values (?, ?)";
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

	private ResultSet getGeneratedKeys() throws SQLException {
		PreparedStatement getGeneratedKeys = null;
		if (getGeneratedKeys == null) getGeneratedKeys = connect.prepareStatement(
				"select last_insert_rowid();");
		return getGeneratedKeys.executeQuery();
	}

	public String showAllUser(){
		String result = "\n";
		try {
			statement = connect.createStatement();
			ResultSet rs = statement.executeQuery( "SELECT * FROM USER;" );
			result += writeResultSet(rs);
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<String> getGCMRegIds(){
		List<String> regIds = new ArrayList<String>();
		try {
			statement = connect.createStatement();
			ResultSet rs = statement.executeQuery( "SELECT gcm_regid FROM GCM;" );
			ResultSetMetaData rm = rs.getMetaData();
			String colName = rm.getColumnName(1);
			System.out.println(colName);
			while (rs.next())
				regIds.add(rs.getString(colName));

			//			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return regIds;

	}

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
	public void close() {
		try {
			if(resultSet != null) resultSet.close();
			if(statement != null) statement.close();
			connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
