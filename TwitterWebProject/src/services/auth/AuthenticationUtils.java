package services.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import database.DBMapper;
import database.DBMapper.QueryType;
import database.DataBaseErrors;
import database.exceptions.CannotConnectToDatabaseException;
import database.exceptions.QueryFailedException;
import services.ServicesTools;
import services.comments.CommentsUtils;
import services.errors.ServerErrors;
import services.followers.FollowerUtils;
import services.user.User;
import services.user.UserUtils;
import utils.Debug;

public class AuthenticationUtils {
	private static Object lock = new Object(); //Key creation lock
	
	
	//Database queries
	//SELECT
	private final static String CHECK_LOGIN_AND_PASSWORD_QUERY 		= "SELECT login FROM users WHERE login = ? and password = SHA2(?, 256);";
	private final static String GET_USER_ID_QUERY 					= "SELECT idusers FROM users WHERE login = ?;";
	private final static String GET_KEY_QUERY 						= "SELECT * FROM `gr3_dupas_gaspar`.`sessions` WHERE `key` = ?;";
	private final static String GET_KEY_FROM_USER_ID_QUERY 			= "SELECT * FROM `gr3_dupas_gaspar`.`sessions` WHERE `user_id` = ?;";
	private final static String GET_KEY_VALIDITY_TIME_QUERY			= "SELECT expiration FROM `gr3_dupas_gaspar`.`sessions` WHERE `key` = ?;";
	private final static String GET_USER_ID_BY_KEY_QUERY			= "SELECT user_id FROM gr3_dupas_gaspar.sessions WHERE `key` = ?;";
	private final static String GET_INFO_USER_BY_USER_ID_QUERY		= "SELECT idusers, login, email, nom, prenom FROM gr3_dupas_gaspar.users WHERE `idusers` = ?;";
	//INSERT
	private final static String ADD_SESSION_QUERY 					= "INSERT INTO `gr3_dupas_gaspar`.`sessions` (`key`, `user_id`, `expiration`, `root`) VALUES (?, ?, ?, ?);";
	//DELETE
	private final static String REMOVE_KEY_BY_USER_ID_QUERY 		= "DELETE FROM `gr3_dupas_gaspar`.`sessions` WHERE `user_id` = ?;";
	private final static String REMOVE_KEY_BY_KEY_QUERY 			= "DELETE FROM `gr3_dupas_gaspar`.`sessions` WHERE `key` = ?;";
	//UPDATE
	private final static String UPDATE_KEY_VALIDITY_QUERY			= "UPDATE `gr3_dupas_gaspar`.`sessions` SET `expiration` = ? WHERE `user_id` = ?;";
	private final static String CHANGE_PASSWORD_QUERY				= "UPDATE `gr3_dupas_gaspar`.`users` SET `password` = SHA2(?, 256) WHERE `user_id` = ?;";
	
	//COLUMN NAME
	private final static String USER_ID_USERS 						= "idusers";
	private final static String KEY_SESSIONS 						= "key";
	private final static String USER_ID_SESSIONS					= "user_id";
	private final static String VALIDITY_SESSIONS 					= "expiration";
	private final static String LOGIN_USERS		 					= "login";
	private final static String EMAIL_USERS		 					= "email";
	private final static String LAST_NAME_USERS		 				= "nom";
	private final static String FIRST_NAME_USERS		 			= "prenom";

	//Key validity duration
	private final static int KEY_VALIDITY_DURATION_HOUR 			= 1;
	private final static int KEY_VALIDITY_DURATION_MINUTE 			= 0;
	private final static int KEY_VALIDITY_DURATION_SECOND 			= 0;

	//Date manipulation
	private final static String DATE_PATTERN = "HH:mm:ss";
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);
	public final static boolean KEY_VALIDITY_METHOD = true;
	
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

	/**
	 * Check a key validity.
	 * @param key
	 * 	Key to check.
	 * @param method
	 * 	Testing method.
	 * 		True : now + validityDuration > duration
	 * 		False : validity < now 
	 * @return
	 * 	True : the key is valid, False = the key is not valid.
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * @throws SQLException
	 * XXX TEST : partial, the second validation checking method has not been tested.
	 */
	public static boolean isKeyValid(String key, boolean method) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		Date validity = null;
		boolean result = false;
		
		//Getting validity value in database.
		ResultSet queryResult = DBMapper.executeQuery(GET_KEY_VALIDITY_TIME_QUERY, QueryType.SELECT, key);
		if (queryResult.next()) {
			validity = DBMapper.parseDate(queryResult.getString(VALIDITY_SESSIONS));
			queryResult.close();
		} else {
			queryResult.close();
			return false;
		}
		
		
		//Validity checking methods
		if (method) {
			Calendar duration = Calendar.getInstance();
			
			duration.setTime(validity);
			
			duration.set(Calendar.HOUR, duration.get(Calendar.HOUR) + KEY_VALIDITY_DURATION_HOUR);
			duration.set(Calendar.MINUTE, duration.get(Calendar.MINUTE) + KEY_VALIDITY_DURATION_MINUTE);
			duration.set(Calendar.SECOND, duration.get(Calendar.SECOND) + KEY_VALIDITY_DURATION_SECOND);
			
			
			result = !(new Date(System.currentTimeMillis()).after(duration.getTime()));
		} else {
			result =  validity.before(new Date(System.currentTimeMillis()));
		}
		
		if(!result) {
			DBMapper.executeQuery(REMOVE_KEY_BY_KEY_QUERY, QueryType.DELETE, key);
		}
		
		return result;
		
	}
	
	/**
	 * Return the key owner's userId.
	 * @param key
	 * 	Key to use.
	 * @return
	 * 	UserId of the key owner. The method will return -1 if the key isn't valid.
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * @throws SQLException
	 * XXX TEST ok
	 */
	public static int getUserIdByKey(String key) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		if (isKeyValid(key, KEY_VALIDITY_METHOD)) {
			ResultSet qResult;
			qResult = DBMapper.executeQuery(GET_USER_ID_BY_KEY_QUERY, QueryType.SELECT, key);
			qResult.next();

			return qResult.getInt(USER_ID_SESSIONS); 
		} else {
			return -1;
		}
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
			if (isKeyValid(result.getString(KEY_SESSIONS), KEY_VALIDITY_METHOD)) {
				DBMapper.executeQuery(UPDATE_KEY_VALIDITY_QUERY, QueryType.UPDATE, DBMapper.getTimeNow(), userID);
				key = result.getString(KEY_SESSIONS);
				Debug.display_notice("Validity update success.");
			}
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
			validity = DBMapper.getTimeNow();
			
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
	 * XXX TEST : not tested
	 */
	public static void removeSession(int userId) throws CannotConnectToDatabaseException, QueryFailedException {
		DBMapper.executeQuery(REMOVE_KEY_BY_USER_ID_QUERY, QueryType.DELETE, userId);
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject generateLoginAnswer(String key) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		JSONObject ret = new JSONObject();
		ret.put(ServicesTools.KEY_ARG, key);
		
		int userId = getUserIdByKey(key);
		JSONObject infoUsers = getInfoUserByUserId(userId).toJSON();
		ret.putAll(infoUsers);
		
		int nbFollows = FollowerUtils.getNbFollows(userId);
		ret.put(ServicesTools.NB_FOLLOWS_ARG, nbFollows);

		int nbFollowers = FollowerUtils.getNbFollowers(userId);
		ret.put(ServicesTools.NB_FOLLOWERS_ARG, nbFollowers);
		
		long nbMsgs = CommentsUtils.getNbMessagesByUserId(userId);
		ret.put(ServicesTools.NB_MESSAGES_ARG, nbMsgs);

		return ret;
	}

	private static User getInfoUserByUserId(int userId) throws SQLException, CannotConnectToDatabaseException, QueryFailedException {
		ResultSet result = DBMapper.executeQuery(GET_INFO_USER_BY_USER_ID_QUERY, QueryType.SELECT, userId);

		if(!result.next())
			return null;
		else {
			User user = new User(result.getInt(USER_ID_USERS),
					result.getString(LOGIN_USERS),
					result.getString(EMAIL_USERS),
					result.getString(FIRST_NAME_USERS),
					result.getString(LAST_NAME_USERS));
			return user;
		}
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
	 * XXX TEST : partial, need to update key validity when user already connected.
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
	
	/**
	 * Change an user's password.
	 * @param key
	 * 	User's authentication key.
	 * @param oldPwd
	 * 	Old password.
	 * @param newPwd
	 * 	New password.
	 * @return
	 * 	JSON answer.
	 * XXX TEST : not done yet
	 */
	public static JSONObject changePassword(String key, String oldPwd, String newPwd) {
		int userId;
		String login;
		
		
		try {
			if (isKeyValid(key, KEY_VALIDITY_METHOD)) {
				
				userId = getUserIdByKey(key); 
				login = UserUtils.getUser(userId).getLogin();
				
				if (checkLoginAndPassword(login, oldPwd)) {
					changePassword(userId, newPwd);
					
					return ServicesTools.generatePositiveAnswer();
				} else {
					return ServicesTools.createJSONError(AuthErrors.BAD_LOGIN_OR_PASSWORD);
				}
				
			} else {
				return ServicesTools.createJSONError(ServerErrors.INVALID_KEY);
			}
		} catch (SQLException e) {
			return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR);
		} catch (CannotConnectToDatabaseException e) {
			return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
		} catch (QueryFailedException e) {
			return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
		}
		
	}
	
	/**
	 * Change an user's password.
	 * @param userId
	 * 	User who needs to change password.
	 * @param newPwd
	 * 	New password.
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * XXX TEST : not done yet
	 */
	private static void changePassword(int userId, String newPwd) throws CannotConnectToDatabaseException, QueryFailedException {
		DBMapper.executeQuery(CHANGE_PASSWORD_QUERY, QueryType.UPDATE, newPwd, userId);
	}


	public static void main(String[] args) {
		System.out.println(login("debug", "password").toJSONString());
	}
	
}
