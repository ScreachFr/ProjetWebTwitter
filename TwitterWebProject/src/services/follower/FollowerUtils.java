package services.follower;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import database.DBMapper;
import database.DBMapper.QueryType;
import database.DataBaseErrors;
import database.exceptions.CannotConnectToDatabaseException;
import database.exceptions.QueryFailedException;
import services.ServicesTools;
import services.auth.AuthenticationUtils;
import services.errors.ServerErrors;
import services.user.UserUtils;

public class FollowerUtils {
	private final static String GET_FOLLOW_BY_COUPLE_ID_QUERY 	= "SELECT * FROM `gr3_dupas_gaspar`.`friends` WHERE `user1` = ? AND `user2` = ?;";
	private final static String GET_FOLLOWERS_QUERY 			= "SELECT user1 FROM `gr3_dupas_gaspar`.`friends` WHERE `user2` = ?;";
	private final static String GET_FOLLOWING_QUERY				= "SELECT user2 FROM `gr3_dupas_gaspar`.`friends` WHERE `user1` = ?;";
	
	private final static String ADD_FOLLOW_QUERY 				= "INSERT INTO `gr3_dupas_gaspar`.`friends` (`user1`, `user2`, `date`) VALUES (?, ?, ?);";
	private final static String REMOVE_FOLLOW_QUERY 			= "DELETE FROM `gr3_dupas_gaspar`.`friends` WHERE `user1` = ? AND `user2` = ?;";

	
	/**
	 * Make an user follow another user.
	 * @param userKey
	 * 	User who need to follow someone.
	 * @param idToFollow
	 * 	User to follow
	 * @return
	 * 	JSON answer
	 * XXX TEST : ok
	 */
	public static JSONObject follow(String userKey, int idToFollow) {
		try {
			//Check key validity
			if(!AuthenticationUtils.isKeyValid(userKey, AuthenticationUtils.KEY_VALIDITY_METHOD)) {
				return ServicesTools.createJSONError(ServerErrors.INVALID_KEY);
			}

			int userId = AuthenticationUtils.getUserIdByKey(userKey);
			
			//Extra precaution
			if(userId < 0) 
				return ServicesTools.createJSONError(ServerErrors.INVALID_KEY);
			else if (userId == idToFollow)
				return ServicesTools.createJSONError(FollowerErrors.SAME_USER_ID);
			else if (!UserUtils.isUserInDB(idToFollow))
				return ServicesTools.createJSONError(FollowerErrors.UNKNOWN_USER);
			else if (isUserFollowing(userId, idToFollow))
				return ServicesTools.createJSONError(FollowerErrors.ALREADY_FOLLOWING);
			
			DBMapper.executeQuery(ADD_FOLLOW_QUERY, QueryType.INSERT, userId, idToFollow, DBMapper.getTimeNow());

			
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
	 * Unfollow an user.
	 * @param userKey
	 * 	User who request the unfollow.
	 * @param idToFollow
	 * 	Id to unfollow.
	 * @return
	 * 	JSON answer.
	 * XXX TEST : ok
	 */
	public static JSONObject unfollow(String userKey, int idToFollow) {
		//Check key validity
		try {
			if(!AuthenticationUtils.isKeyValid(userKey, AuthenticationUtils.KEY_VALIDITY_METHOD)) {
				return ServicesTools.createJSONError(ServerErrors.INVALID_KEY);
			}
			int userId = AuthenticationUtils.getUserIdByKey(userKey);
			
			//Extra precaution
			if(userId < 0) 
				return ServicesTools.createJSONError(ServerErrors.INVALID_KEY);
			else if (userId == idToFollow)
				return ServicesTools.createJSONError(FollowerErrors.SAME_USER_ID);
			else if (!UserUtils.isUserInDB(idToFollow))
				return ServicesTools.createJSONError(FollowerErrors.UNKNOWN_USER);
			else if (!isUserFollowing(userId, idToFollow))
				return ServicesTools.generatePositiveAnswer();
			
			DBMapper.executeQuery(REMOVE_FOLLOW_QUERY, QueryType.DELETE, userId, idToFollow);

			
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
	 * Return all users followers ids.
	 * @param idUser
	 * 	Id to use.
	 * @return
	 * 	JSON answer.
	 * XXX TEST : ok
	 */
	public static JSONObject getUsersFollowers(int idUser) {
		JSONObject result = new JSONObject();
		ArrayList<Integer> followers;
		
		try {
			followers = getFollowers(idUser);
			
			result.put("followers", followers);
			
			return result;
		}  catch (SQLException e) {
			return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR);
		} catch (CannotConnectToDatabaseException e) {
			return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
		} catch (QueryFailedException e) {
			return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
		}
		
	}
	
	/**
	 * Return all userIds of users following.
	 * @param idUser
	 * 	Id to use.
	 * @return
	 * 	JSON answer.
	 * XXX TEST : ok
	 */
	public static JSONObject getUsersFollows(int idUser) {
		JSONObject result = new JSONObject();
		ArrayList<Integer> followers;
		
		try {
			followers = getFollowing(idUser);
			
			result.put("follows", followers);
			
			return result;
		}  catch (SQLException e) {
			return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR);
		} catch (CannotConnectToDatabaseException e) {
			return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
		} catch (QueryFailedException e) {
			return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
		}
		
	}
	
	
	/**
	 * Check if user1 is following user2.
	 * @param idUser1
	 * 	User1 id.
	 * @param idUser2
	 * 	User2 id.
	 * @return
	 * 	True = 1 is following 2. False = 1 isn't following 2.
	 * @throws SQLException
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * XXX TEST : ok
	 */
	private static boolean isUserFollowing(int idUser1, int idUser2) throws SQLException, CannotConnectToDatabaseException, QueryFailedException {
		ResultSet result = DBMapper.executeQuery(GET_FOLLOW_BY_COUPLE_ID_QUERY, QueryType.SELECT, idUser1, idUser2);
		boolean found = result.next();
		result.close();
		
		return found;
	}
	
	/**
	 * Returns users who follow idUser.
	 * @param idUser
	 * 	Id to use.
	 * @return
	 * 	List of followers.
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * @throws SQLException
	 * XXX TEST : ok
	 */
	private static ArrayList<Integer> getFollowers(int idUser) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		return getFollowList(idUser, GET_FOLLOWERS_QUERY);
	}
	
	/**
	 * Returns users who idUser follows.
	 * @param idUser
	 * 	Id to use.
	 * @return
	 * 	List of followers.
	 * @throws CannotConnectToDatabaseException
	 * @throws QueryFailedException
	 * @throws SQLException
	 * XXX TEST : ok
	 */
	private static ArrayList<Integer> getFollowing(int idUser) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		return getFollowList(idUser, GET_FOLLOWING_QUERY);
	}
	
	/**
	 * This method fetch a a list of users and return it as an ArrayList. It must be used with GET_FOLLOWING_QUERY and GET_FOLLOWERS_QUERY only.
	 */
	private static ArrayList<Integer> getFollowList(int idUser, String query) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		ArrayList<Integer> result = new ArrayList<>();
		ResultSet qResult;
		
		qResult = DBMapper.executeQuery(query, QueryType.SELECT, idUser);
		
		while (qResult.next()) {
			result.add(qResult.getInt(1));
		}
		
		return result;
	}
	
}
