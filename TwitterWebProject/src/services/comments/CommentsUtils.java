package services.comments;

import java.sql.SQLException;

import org.bson.Document;
import org.json.simple.JSONObject;

import services.ServicesTools;
import services.auth.AuthenticationUtils;
import services.user.User;
import services.user.UserUtils;
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
			userId = AuthenticationUtils.getUserIdByKey(key);
			crtUser = UserUtils.getUser(userId);

			Document com2 = new Document();
			
			com2.put(USER_ID_MONGO, crtUser.getId());
			com2.put(DATE_MONGO, DBMapper.getTimeNow());
			com2.put(AUTHOR_LOGIN_MONGO, crtUser.getLogin());
			com2.put(CONTENT_MONGO, content);
			
			MongoMapper.executeInsertOne(COMMENT_COLLECTION_NAME, com2);
		
			return ServicesTools.generatePositiveAnswer();
			
		} catch (SQLException e) {
			return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR);
		} catch (CannotConnectToDatabaseException e) {
			return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
		} catch (QueryFailedException e) {
			return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
		}
		
		
		
		
	}
	
	public static void main(String[] args) {
//		System.out.println(AuthenticationUtils.login("debug", "password"));
		
		String key = "672953b7552449bea509ea7889226585";
		
		System.out.println(CommentsUtils.addComment(key, "Hello world!"));
		
		
	}
}





