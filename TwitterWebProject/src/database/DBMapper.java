package database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author alexandre
 * DBMapper.java
 * Database connection.
 */
public class DBMapper {
	private final static String HOST = "132.227.201.129";
	private final static String PORT = "33306";
	private final static String DATABASE = "gr3_dupas_gaspar";
	private final static String LOGIN = "gr3_dupas_gaspar";
	private final static String PASSWORD = "7XRMfn";
	
	private final static String DATE_PATTERN = "HH:mm:ss dd/MM/YY";
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);
	
	
	public static Connection getMySQLConnection() throws SQLException {

		return DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE,	LOGIN, PASSWORD);
	}
	
	
	public static ResultSet executeQuery(String query, Object... args) throws SQLException {
		Connection database = getMySQLConnection();
		
		if (database == null) {
			return null;
		}
		
		try {
			PreparedStatement stat = database.prepareStatement(query);
			
			for (int i = 0; i < args.length; i++) 
				stat.setObject(i+1, args[i]);
			
			return stat.executeQuery();
			
		} catch (SQLException e) {
			throw e;
		}
		
	}
	
	/**
	 * Return current time. This method is in DBMapper to be sure it's use with database interactions. 
	 * The used pattern is "HH:mm:ss dd/MM/YY".
	 */
	public static String getTime() {
		
		return DATE_FORMAT.format(new Date(System.currentTimeMillis()));
	}
	
	/**
	 * Parse a string to a date. Caution : this method is made to parse date from data base who follow the "HH:mm:ss dd/MM/YY" pattern.
	 * @param s
	 * 	String to parse.
	 * @return
	 * 	Date from parsed string.
	 */
	public static Date parseDate(String s) {
		
		try {
			return DATE_FORMAT.parse(s);
		} catch (ParseException e) {
			System.err.println("ERROR : Failed to parse time.");
			return null;
		}
	}
	
}

