package services.comments;

import java.sql.SQLException;

import org.bson.Document;
import org.json.simple.JSONObject;

import services.ServicesTools;
import services.auth.AuthErrors;
import services.auth.AuthenticationUtils;
import services.errors.ServerErrors;
import services.user.User;
import services.user.UserUtils;
import utils.Debug;
import database.DBMapper;
import database.DataBaseErrors;
import database.MongoMapper;
import database.exceptions.CannotConnectToDatabaseException;
import database.exceptions.QueryFailedException;

public class CommentsUtils {
	private final static String USER_ID_MONGO = "userid";
	private final static String DATE_MONGO = "date";
	private final static String AUTHOR_LOGIN_MONGO = "author_login";
	private final static String CONTENT_MONGO = "content";
	private final static String COMMENT_COLLECTION_NAME = "comments";
	
	
	public static JSONObject addComment(String key, String content) {
		User crtUser;
		int userId;
		
		
		try {
			
			
			if (!AuthenticationUtils.isKeyValid(key, AuthenticationUtils.KEY_VALIDITY_METHOD))
				return ServicesTools.createJSONError(ServerErrors.INVALID_KEY);
			
			userId = AuthenticationUtils.getUserIdByKey(key);
			crtUser = UserUtils.getUser(userId);

			putComment(userId, DBMapper.getTimeNow(), crtUser.getLogin(), content);
			
			return ServicesTools.generatePositiveAnswer();
			
		} catch (SQLException e) {
			return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR);
		} catch (CannotConnectToDatabaseException e) {
			return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
		} catch (QueryFailedException e) {
			return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
		}
		
	}
	
	private static void putComment(int userId, String time, String authorLogin, String content) throws SQLException {
		Document doc = new Document();
		
		doc.put(USER_ID_MONGO, userId);
		doc.put(DATE_MONGO, time);
		doc.put(AUTHOR_LOGIN_MONGO, authorLogin);
		doc.put(CONTENT_MONGO, content);
		
		MongoMapper.executeInsertOne(COMMENT_COLLECTION_NAME, doc);
	}
	
	
	public static void main(String[] args) {
//		System.out.println(AuthenticationUtils.login("debug", "password"));
		
		String key = "b293d9f187a14182b6b21914c7f86881";
		
		System.out.println(CommentsUtils.addComment(key, "Hello world!"));
		
		
	}
}





