package services.comments;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import services.ServicesTools;
import services.auth.AuthErrors;
import services.auth.AuthenticationUtils;
import services.errors.ServerErrors;
import services.followers.FollowerUtils;
import services.user.User;
import services.user.UserUtils;
import utils.Debug;
import database.DBMapper;
import database.DataBaseErrors;
import database.MongoMapper;
import database.MongoMapper.Operator;
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
		} catch (CannotConnectToDatabaseException e) {
			return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
		} catch (QueryFailedException e) {
			return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
		}
	}

	public static JSONObject getCommentsDependsOnTime(String date, int maxResult, Operator op, int idUser) {
		JSONObject answer = new JSONObject();
		ArrayList<Comment> comments;
		ArrayList<JSONObject> jsonComments = new ArrayList<>();
		try {
		switch (op) {
			case GT:
				comments = getCommentsAfter(date, maxResult, idUser);
				break;
			case LT:
				comments = getCommentsBefore(date, maxResult, idUser);
				break;
			default:
				return ServicesTools.createJSONError(ServerErrors.BAD_ARGUMENT);
			}
		} catch (SQLException e) {
			return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR); 
		} catch (CannotConnectToDatabaseException e) {
			return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
		} catch (QueryFailedException e) {
			return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
		}
		
		for (Comment comment : comments) {
			jsonComments.add(comment.toJSON());
		}
		
		answer.put(COMMENT_COLLECTION_NAME, jsonComments);
		return answer;
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

	private static ArrayList<Comment> fetchComments(int userId, int page, int nbPerPage) throws SQLException, CannotConnectToDatabaseException, QueryFailedException {
		int startIndex;
		FindIterable<Document> qResult;
		Map<String, Object> args = new HashMap<>();

		if(userId != -1)
			args.put(USER_ID_MONGO, userId);
		startIndex = page * nbPerPage;

		qResult = MongoMapper.executeGet(COMMENT_COLLECTION_NAME, args, startIndex);

		

		return commentsFromDocumentsToArrayList(qResult, nbPerPage, -1);
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

	public static ArrayList<Comment> getCommentsAfter(String date, int maxResult, int idUser) throws SQLException, CannotConnectToDatabaseException, QueryFailedException {
		return getCommentsDependsOnDate(date, maxResult, Operator.GT.toString(), idUser);
	}
	
	public static ArrayList<Comment> getCommentsBefore(String date, int maxResult, int idUser) throws SQLException, CannotConnectToDatabaseException, QueryFailedException {
		return getCommentsDependsOnDate(date, maxResult, Operator.LT.toString(), idUser);
	}
	
	private static ArrayList<Comment> getCommentsDependsOnDate(String date, int maxResult, String operator, int idUser) throws SQLException, CannotConnectToDatabaseException, QueryFailedException {
		Map<String, Object> args = new HashMap<>();
		Map<String, Object> sortArgs = new HashMap<>();
		FindIterable<Document> qResult;

		Debug.display_notice(date);
		
		args.put(DATE_MONGO, new BasicDBObject(operator, date));
		sortArgs.put(DATE_MONGO, -1);
		
		qResult = MongoMapper.executeGetWSort(COMMENT_COLLECTION_NAME, args, sortArgs, 0);

		return commentsFromDocumentsToArrayList(qResult, maxResult, idUser);
	}
	
	private static ArrayList<Comment> commentsFromDocumentsToArrayList(FindIterable<Document> qResult, int maxResult, int idUser) throws SQLException, CannotConnectToDatabaseException, QueryFailedException {
		ArrayList<Comment> result = new ArrayList<>();
		int i = 0;
		
		
		for (Document document : qResult) {
			if( idUser == -1 || FollowerUtils.isUserFollowing(idUser, document.getInteger(USER_ID_MONGO, -1)) || idUser == document.getInteger(USER_ID_MONGO, -1)) {
			result.add(new Comment(document.get(COMMENT_ID_MONGO).toString(), document.getInteger(USER_ID_MONGO, 0), document.getString(AUTHOR_LOGIN_MONGO), DBMapper.parseDate(document.getString(DATE_MONGO)),
					document.getString(CONTENT_MONGO)));
			i++;
			if(i >= maxResult)
				break;
			}
		}
		
		return result;
	}
	
	public static Comment getComment(String commentId) throws SQLException {
		Comment result;
		Map<String, Object> args = new HashMap<>();
		FindIterable<Document> qResult;
		Document document = null;
		args.put(COMMENT_ID_MONGO, new ObjectId(commentId));
		
		qResult = MongoMapper.executeGet(COMMENT_COLLECTION_NAME, args, 0);
		
		document = qResult.first();
		
		
		try {
			result = new Comment(document.get(COMMENT_ID_MONGO).toString(), document.getInteger(USER_ID_MONGO, 0), document.getString(AUTHOR_LOGIN_MONGO), DBMapper.parseDate(document.getString(DATE_MONGO)),
					document.getString(CONTENT_MONGO));
		} catch (NullPointerException  e) {
			return null;
		}
		
		return result;
	}
	
	
	
	public static void main(String[] args) {
//		String key = (String) AuthenticationUtils.login("debug", "password").get("key");

//		System.out.println(addComment(key, "test 5"));

//		System.out.println(getComments(-1, 0, 15));

		try {
			System.out.println(getComment("570543129966cc5c436fdaf5"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(getCommentsDependsOnTime(DBMapper.getTimeNow(), 5, Operator.LT, -1));
	}
}





