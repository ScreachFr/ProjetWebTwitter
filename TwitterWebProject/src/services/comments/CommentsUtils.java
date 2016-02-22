package services.comments;

import java.sql.SQLException;

import org.bson.Document;
import org.json.simple.JSONObject;

import services.ServicesTools;
import database.MongoMapper;

public class CommentsUtils {

	public static JSONObject addComment(String userId, String authorLogin,
			String text) {
		
		Document com2 = new Document();
		
		com2.put("user_id", 1);
		com2.put("date", MongoMapper.getTime()); //TODO : envoi d'un type Date ?
		com2.put("author_login", "1");
		com2.put("text", "blabla");
		
		try {
			MongoMapper.executeInsertOne("comments", com2);
		} catch (SQLException e) {
			return ServicesTools.createJSONError(null); //TODO
		}
		return null;
	}
}
