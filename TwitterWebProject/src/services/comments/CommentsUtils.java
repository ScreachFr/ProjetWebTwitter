package services.comments;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.simple.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

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
	public final static String COMMENT_ID_MONGO = "_id";
	public final static String USER_ID_MONGO = "userid";
	public final static String DATE_MONGO = "date";
	public final static String AUTHOR_LOGIN_MONGO = "author_login";
	public final static String CONTENT_MONGO = "content";
	public final static String COMMENT_COLLECTION_NAME = "comments";
	
	public final static String COMMENT_JSON_ANSWER = "comment"; 
	
	
	/**
	 * Add a comment.
	 * @param key
	 * 	User's key.
	 * @param content
	 * 	Content of the comment.
	 * @return
	 */
	public static JSONObject addComment(String key, String content) {
		User crtUser;
		int userId;
		JSONObject answer;
		Comment c;
		
		try {
			if (!AuthenticationUtils.isKeyValid(key, AuthenticationUtils.KEY_VALIDITY_METHOD))
				return ServicesTools.createJSONError(ServerErrors.INVALID_KEY);
			
			userId = AuthenticationUtils.getUserIdByKey(key);
			crtUser = UserUtils.getUser(userId);

			c = putComment(userId, DBMapper.getTimeNow(), crtUser.getLogin(), content);
			
			answer =  ServicesTools.generatePositiveAnswer();
			answer.put(COMMENT_JSON_ANSWER, c.toJSON());
			
			return answer;
		} catch (SQLException e) {
			return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR);
		} catch (CannotConnectToDatabaseException e) {
			return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
		} catch (QueryFailedException e) {
			return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
		}
		
	}
	
	/**
	 * Returns comments of an user.
	 * @param userId
	 * 	User to use.
	 * @param page
	 * 	Page number. Start at 0.
	 * @param nbPerPage
	 * 	Amount of comment per page.
	 * @return
	 * 	JSON answer.
	 */
	public static JSONObject getComments(int userId, int page, int nbPerPage) {
		JSONObject result = new JSONObject();
		ArrayList<Comment> comments;
		ArrayList<JSONObject> jsonComments = new ArrayList<>();
		
		try {
			comments = fetchComments(userId, page, nbPerPage);
			for (Comment comment : comments) {
				jsonComments.add(comment.toJSON());
			}

			result.put(COMMENT_COLLECTION_NAME, jsonComments);
			return result;
			
		} catch (SQLException e) {
			return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR);
		}
	}
	
	private static Comment putComment(int userId, String time, String authorLogin, String content) throws SQLException {
		Comment result;
		Document doc = new Document();
				
		doc.put(USER_ID_MONGO, userId);
		doc.put(DATE_MONGO, time);
		doc.put(AUTHOR_LOGIN_MONGO, authorLogin);
		doc.put(CONTENT_MONGO, content);
		
		MongoMapper.executeInsertOne(COMMENT_COLLECTION_NAME, doc);
		
		FindIterable<Document> qResult;
		Map<String, Object> args = new HashMap<>();

		args.put(USER_ID_MONGO, userId);
		args.put(DATE_MONGO, time);
		
		qResult = MongoMapper.executeGet(COMMENT_COLLECTION_NAME, args, 0);
		
		doc = qResult.first();
		
		result = new Comment(doc.get(COMMENT_ID_MONGO).toString(), doc.getInteger(USER_ID_MONGO, 0), doc.getString(AUTHOR_LOGIN_MONGO), DBMapper.parseDate(doc.getString(DATE_MONGO)),
				doc.getString(CONTENT_MONGO));
		
		
		return result;
	}
	
	private static ArrayList<Comment> fetchComments(int userId, int page, int nbPerPage) throws SQLException {
		ArrayList<Comment> result = new ArrayList<>();
		int startIndex;
		FindIterable<Document> qResult;
		Map<String, Object> args = new HashMap<>();
		
		if(userId != -1)
			args.put(USER_ID_MONGO, userId);
		startIndex = page * nbPerPage;
		
		qResult = MongoMapper.executeGet(COMMENT_COLLECTION_NAME, args, startIndex);
		
		int i = 0;
		for (Document document : qResult) {
			result.add(new Comment(document.get(COMMENT_ID_MONGO).toString(), document.getInteger(USER_ID_MONGO, 0), document.getString(AUTHOR_LOGIN_MONGO), DBMapper.parseDate(document.getString(DATE_MONGO)),
					document.getString(CONTENT_MONGO)));
			
			
			i++;
			if(i >= nbPerPage)
				break;
		}
		
		
		return result;
	}
	
	
	public static long getNbCommentsByUserId(int userId) throws SQLException {		
		MongoDatabase database = MongoMapper.getMongoDBConnection();
		MongoCollection<Document> collect = database.getCollection(COMMENT_COLLECTION_NAME);
		BasicDBObject whereQuery = new BasicDBObject();


		Map<String, Object> args = new HashMap<>();
		
		args.put(USER_ID_MONGO, userId);
		whereQuery.putAll(args);
		
		return collect.count(whereQuery);
	}
	
	public static void main(String[] args) {
		String key = (String) AuthenticationUtils.login("debug", "password").get("key");
		
		System.out.println(addComment(key, "test 5"));
		
//		System.out.println(getComments(-1, 0, 15));
		
	}
}





