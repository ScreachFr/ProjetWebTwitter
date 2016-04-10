package database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import database.exceptions.CannotConnectToDatabaseException;
import database.exceptions.QueryFailedException;
import utils.Debug;


/**
 * @author alexandre
 * DBMapper.java
 * Database connection.
 */
public class DBMapper {
	

	//private final static String DATE_PATTERN = "HH:mm:ss dd/MM/YY"; //postgres
	public final static String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"; //mysql
	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);
	
	public final static String JDBC_CLASS = "com.mysql.jdbc.Driver";

	private static Connection crtConnection;
	
	public final static int DUPLICATE_P_KEY_ERROR_CODE = 1062;
	
	
	public static Connection getMySQLConnection() throws SQLException {
		try {
			Class.forName(JDBC_CLASS);
		} catch (ClassNotFoundException e) {
			Debug.display_stack(e);
		}
		
		if (crtConnection == null)
			crtConnection = DriverManager.getConnection("jdbc:mysql://" + DBSettings.HOST + ":" + DBSettings.PORT + "/" + DBSettings.DATABASE,
					DBSettings.LOGIN, DBSettings.PASSWORD);
		
		return crtConnection;
	}


	public static ResultSet executeQuery(String query,QueryType type, Object... args) 
			throws CannotConnectToDatabaseException, QueryFailedException {

		Connection database;
		ResultSet result = null;


		try {
			database = getMySQLConnection();
		} catch (SQLException e1) {
			Debug.display_stack(e1);
			throw new CannotConnectToDatabaseException();
		}

		try {
			PreparedStatement stat = database.prepareStatement(query);
			//Fill args
			for (int i = 0; i < args.length; i++) 
				stat.setObject(i+1, args[i]);

			switch (type) {
			case SELECT:
				result = stat.executeQuery();
				break;
			default: //The Select is the only action that returns a resultSet.
				stat.executeUpdate();
				break;
			}

			return result;

		} catch (SQLException e) {
			throw new QueryFailedException(e);
		}

	}


	/**
	 * Return current time. This method is in DBMapper to be sure it's use with database interactions. 
	 * The used pattern is "HH:mm:ss dd/MM/YY".
	 */
	public static String getTimeNow() {

		return DATE_FORMAT.format(new Date(System.currentTimeMillis()));
	}

	/**
	 * Parse a string to a date. Caution : this method has been made to parse date from data base who follow the "yyyy-MM-dd HH:mm:ss" pattern.
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

	public enum QueryType {
		SELECT, UPDATE, DELETE, INSERT;
	}

	public static void main(String[] args) {
		System.out.println(getTimeNow());
	}

}

