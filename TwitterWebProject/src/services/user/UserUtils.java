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
import services.errors.ServerErrors;

public class UserUtils {
	private final static String INSERT_USER_QUERY = "INSERT INTO users (idusers, login, password, email) VALUES (DEFAULT, ?, SHA2(?, 256), ?);";
	private final static String CHECK_LOGIN_QUERY = "SELECT login FROM users WHERE login = ? OR email = ?;";

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
	public static JSONObject createUser(String login, String password, String email) {
		if (ServicesTools.nullChecker(login, password))
			return ServicesTools.createJSONError(ServerErrors.MISSING_ARGUMENT);
		else
			try {
				if (checkLoginAndEmail(login, email)) {
					return ServicesTools.createJSONError(UserErrors.LOGIN_OR_EMAIL_ALREADY_EXIST);
				} else
					UserUtils.addUser(login, password, email);
				return getCreateUserResponse(); 
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
	private static void addUser(String login, String password, String email) 
			throws SQLException, CannotConnectToDatabaseException, QueryFailedException {

		DBMapper.executeQuery(INSERT_USER_QUERY, QueryType.INSERT, login, password, email);
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
	 * Return a positive response.
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject getCreateUserResponse() {
		JSONObject ret = new JSONObject();

		ret.put("success", true);

		return ret;
	}

}
