package services.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONObject;

import database.DBMapper;
import database.DataBaseErrors;
import database.DBMapper.QueryType;
import database.exceptions.CannotConnectToDatabaseException;
import database.exceptions.QueryFailedException;
import services.ServicesTools;
import services.comments.CommentsUtils;
import services.errors.ServerErrors;
import services.followers.FollowerUtils;

public class UserUtils {
	private final static String INSERT_USER_QUERY 				= "INSERT INTO users (idusers, login, password, email, prenom, nom) VALUES (DEFAULT, ?, SHA2(?, 256), ?, ?, ?);";
	private final static String CHECK_LOGIN_QUERY 				= "SELECT login FROM users WHERE login = ? OR email = ?;";
	private final static String GET_USER_INFO_BY_ID_QUERY 		= "SELECT * FROM users WHERE idusers = ?;";
	
	private final static String LOGIN_ATTR_NAME = "login";
	private final static String EMAIL_ATTR_NAME = "email";
	private final static String FIRSTNAME_ATTR_NAME = "prenom";
	private final static String LASTNAME_ATTR_NAME = "nom";
	

	/**
	 * Create an user.
	 * @param login
	 * 	Login of the new user.
	 * @param password
	 * 	Password of the new user.
	 * @param email
	 * 	Email of the new user.
	 * @return
	 * 	JSON response.
	 *  XXX TEST : ok
	 */
	public static JSONObject createUser(String login, String password, String email, String firstName, String lastName) {
		if (ServicesTools.nullChecker(login, password))
			return ServicesTools.createJSONError(ServerErrors.MISSING_ARGUMENT);
		else
			try {
				if (checkLoginAndEmail(login, email)) {
					return ServicesTools.createJSONError(UserErrors.LOGIN_OR_EMAIL_ALREADY_EXIST);
				} else
					UserUtils.addUser(login, password, email, firstName, lastName);
				return ServicesTools.generatePositiveAnswer(); 
			}  catch (SQLException e) {
				return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR);
			} catch (CannotConnectToDatabaseException e) {
				return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
			} catch (QueryFailedException e) {
				return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
			}
	}

	/**
	 * Add an user to the database. 
	 * @param login
	 * 	Login of the new user.
	 * @param password
	 * 	Password of the new user.
	 * @param email
	 * 	Email of the new user.
	 * @throws SQLException
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * XXX TEST : ok
	 */
	private static void addUser(String login, String password, String email, String firstName, String lastName) 
			throws SQLException, CannotConnectToDatabaseException, QueryFailedException {

		DBMapper.executeQuery(INSERT_USER_QUERY, QueryType.INSERT, login, password, email, firstName, lastName);
	}
	
	/**
	 * Test if a login already exist in database.
	 * @param login
	 * 	Login to test.
	 * @param email
	 * 	Email to test.
	 * @return
	 * 	true = the login already exists. false = the login does not exists.
	 * @throws SQLException
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * XXX TEST : ok
	 */
	public static boolean checkLoginAndEmail(String login, String email) 
			throws SQLException, CannotConnectToDatabaseException, QueryFailedException {
		ResultSet result = DBMapper.executeQuery(CHECK_LOGIN_QUERY, QueryType.SELECT, login, email);
		boolean found = result.next();
		result.close();
		
		return found;
	}
	
	
	/**
	 * Check if a user id is in the database.
	 * @param userId
	 * 	Id to check.
	 * @return
	 * 	True = the id is in db. False = the id is not in db.
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * @throws SQLException
	 * XXX TEST : ok
	 */
	public static boolean isUserInDB(int userId) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		ResultSet result = DBMapper.executeQuery(GET_USER_INFO_BY_ID_QUERY, QueryType.SELECT, userId);
		boolean found = result.next();
		result.close();
		
		return found;
		
		
	}
	
	public static User getUser(int userId) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		User result = null;
		ResultSet qResult;
		String lastName, firstName, login, email;
		
		qResult = DBMapper.executeQuery(GET_USER_INFO_BY_ID_QUERY, QueryType.SELECT, userId);
		
		if(!qResult.next())
			return null;
		
		lastName = qResult.getString(LASTNAME_ATTR_NAME);
		firstName = qResult.getString(FIRSTNAME_ATTR_NAME);
		login = qResult.getString(LOGIN_ATTR_NAME);
		email = qResult.getString(EMAIL_ATTR_NAME);
		
		result = new User(userId, login, email, firstName, lastName);
		
		return result;
	}
	
	/**
	 * Get an user and convert it to JSONObject.
	 * @param userId
	 * 	User's id.
	 * @param askingUserId
	 * 	Id of the user who makes request. Will define contact. If you don't want to check this, put that value to -1.
	 * @return
	 * 	JSON answer.
	 * XXX Test : ok
	 */
	public static JSONObject getUserToJSON(int userId, int askingUserId, boolean includeStats) {
		JSONObject result;
		User user;
		try {
			user = getUser(userId);
			
			if(user == null)
				return ServicesTools.createJSONError(UserErrors.UNKNOWN_USER);
			
			if(askingUserId > 0) {
				user.setContact(FollowerUtils.isUserFollowing(askingUserId, userId));
			}
			
			result = user.toJSON();

			if(includeStats)
				result.put(Stats.CLASS_JSON, getStats(userId).toJSON());
		}  catch (SQLException e) {
			return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR);
		} catch (CannotConnectToDatabaseException e) {
			return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
		} catch (QueryFailedException e) {
			return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
		}
		return result;
	}
	
	public static Stats getStats(int userId) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		int nbFollows = FollowerUtils.getNbFollows(userId);
		int nbFollowers = FollowerUtils.getNbFollowers(userId);
		long nbComments = CommentsUtils.getNbCommentsByUserId(userId);
		
		return new Stats(nbFollows, nbFollowers, nbComments);
	}
	
	public static void main(String[] args) {
		System.out.println(getUserToJSON(6, 5, true));
	}
}
