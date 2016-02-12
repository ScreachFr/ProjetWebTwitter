package services.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


import org.json.simple.JSONObject;

import database.DBMapper;
import database.DBMapper.QueryType;
import database.DataBaseErrors;
import database.exceptions.CannotConnectToDatabaseException;
import database.exceptions.QueryFailedException;
import services.ServicesTools;
import services.errors.ServerErrors;

public class AuthenticationUtils {
	private static Object lock = new Object();
	
	//SELECT
	private final static String CHECK_LOGIN_AND_PASSWORD_QUERY 	= "SELECT login FROM users WHERE login = ? and password = SHA2(?, 256);";
	private final static String GET_USER_ID_QUERY 				= "SELECT iduser FROM users WHERE login = ?;";
	private final static String GET_KEY_QUERY 					= "SELECT * FROM `gr3_dupas_gaspar`.`sessions` WHERE `key` = ?;";
	private final static String GET_KEY_FROM_USER_ID_QUERY 		= "SELECT * FROM `gr3_dupas_gaspar`.`sessions` WHERE `iduser` = ?;";
	//INSERT
	private final static String ADD_SESSION_QUERY 				= "INSERT INTO `gr3_dupas_gaspar`.`sessions` (`key`, `iduser`, `validity`, `admin`) VALUES (?, ?, ?, ?);";
	//DELETE
	private final static String REMOVE_KEY_BY_USER_ID_QUERY 	= "DELETE FROM `gr3_dupas_gaspar`.`sessions` WHERE `iduser` = ?;";
	//COLUMN NAME
	private final static String USER_ID_USERS = "iduser";
	private final static String KEY_SESSIONS = "key";


	/**
	 * Check if login and password match.
	 * @param login
	 * 	Login to test.
	 * @param password
	 * 	Password to test.
	 * @return
	 * 	True = the login and password match. False = the login and password does not match.
	 * @throws SQLException
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * XXX TEST : ok
	 */
	public static boolean checkLoginAndPassword(String login, String password) throws SQLException, CannotConnectToDatabaseException, QueryFailedException {
		ResultSet result = DBMapper.executeQuery(CHECK_LOGIN_AND_PASSWORD_QUERY, QueryType.SELECT, login, password);
		boolean found = result.next();
		result.close();
		return found;
	}

	//TODO
	public static boolean isKeyValid(String key) {
		return true;
	}

	/**
	 * Get session key from userId.
	 * @param userID
	 * 	Used id.
	 * @return
	 * 	User's key. This method will return null if the session is not valid anymore or does not exist.
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * @throws SQLException
	 * 	XXX TEST : ok
	 */
	public static String getKeyFromUserId(int userID) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		ResultSet result = DBMapper.executeQuery(GET_KEY_FROM_USER_ID_QUERY, QueryType.SELECT, userID);
		boolean found = result.next();
		String key = null;
		
		if(found) {
			if (isKeyValid(result.getString(KEY_SESSIONS)))
				key = result.getString(KEY_SESSIONS);
		}
		
		result.close();
		return key;
	}
	
	/**
	 * Add a session.
	 * @param userId
	 * 	User id.
	 * @return
	 * 	Authentication key.
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * @throws SQLException
	 * XXX TEST : partial
	 */
	public static String addSession(int userId) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		String key = null;
		String validity = null;
		
		//Will return the user's key if he's already connected.
		key = getKeyFromUserId(userId);
		if (key != null)
			return key;
		
		//Must be synchronized to avoid key duplication.
		synchronized (lock) {
			do {
				key = UUID.randomUUID().toString().replace("-", "");
			} while(isDBContainsKey(key));
			validity = DBMapper.getTime();
			
			DBMapper.executeQuery(ADD_SESSION_QUERY, QueryType.INSERT, key, userId, validity, 0);
		}
		
		return key;
	}

	/**
	 * Check if the database contains a key.
	 * @param key
	 * 	Key to check.
	 * @return
	 * 	True = the database contains the key. False = the database does not contains the key.
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * @throws SQLException
	 * XXX TEST : ok
	 */
	private static boolean isDBContainsKey(String key) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		ResultSet result = DBMapper.executeQuery(GET_KEY_QUERY, QueryType.SELECT, key);
		boolean found = result.next();
		result.close();
		
		return found;
	}
	
	
	/**
	 * Return the id of an user.
	 * @param login
	 * 	Login of the user.
	 * @return
	 * 	The id of the user. If the user is not in the database this method will return -1.
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * @throws SQLException
	 * XXX TEST : ok
	 */
	public static int getUserId(String login) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		ResultSet result = DBMapper.executeQuery(GET_USER_ID_QUERY, QueryType.SELECT, login);

		if(!result.next())
			return -1;
		else
			return result.getInt(USER_ID_USERS);
	}
	
	/**
	 * Remove a key from database.
	 * @param userId
	 * 	UserId of the user's key.
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * XXX TEST : ok
	 */
	public static void removeSession(int userId) throws CannotConnectToDatabaseException, QueryFailedException {
		DBMapper.executeQuery(REMOVE_KEY_BY_USER_ID_QUERY, QueryType.DELETE, userId);
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject generateLoginAnswer(String key) {
		JSONObject ret = new JSONObject();

		ret.put("key", key);

		return ret;
	}

	//TODO find out why this method creates warning. 
	@SuppressWarnings("unchecked")
	public static JSONObject generateLogoutAnswer() {
		JSONObject ret = new JSONObject();

		ret.put("success", true);

		return ret;
	}

	/**
	 * Connect an user.
	 * @param login
	 * 	User's login.
	 * @param password
	 * 	User's password.
	 * @return
	 * 	Authentication key in JSON if it works.
	 * XXX TEST : ok
	 */
	public static JSONObject login (String login, String password) {
		if (ServicesTools.nullChecker(login, password))
			return ServicesTools.createJSONError(ServerErrors.MISSING_ARGUMENT);
		else {

			try {
				if (AuthenticationUtils.checkLoginAndPassword(login, password)) {
					String key;
					int userId;

					userId = AuthenticationUtils.getUserId(login);
					key = AuthenticationUtils.addSession(userId);
					return AuthenticationUtils.generateLoginAnswer(key);
				} else
					return ServicesTools.createJSONError(AuthErrors.BAD_LOGIN_OR_PASSWORD);
			} catch (SQLException e) {
				return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR);
			} catch (CannotConnectToDatabaseException e) {
				return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
			} catch (QueryFailedException e) {
				return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
			}

		}

	}

	/**
	 * Disconnect an user.
	 * @param login
	 * 	User's login
	 * @return
	 * 	Confirmation in JSON if it works.
	 */
	public static JSONObject logout (String login) {
		int userId;

		try {

			userId = AuthenticationUtils.getUserId(login);
			AuthenticationUtils.removeSession(userId);

		} catch (SQLException e) {
			return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR);
		} catch (CannotConnectToDatabaseException e) {
			return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
		} catch (QueryFailedException e) {
			return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
		}


		return AuthenticationUtils.generateLogoutAnswer();
	}

	public static void main(String[] args) {
//		System.out.println(UserUtils.createUser("debug", "password", "mail").toJSONString());
//		System.out.println(login("debug", "password").toJSONString());//3235afb707a9471c8e6fc1e6097143c1
//		System.out.println(logout("debug"));
	}

}
