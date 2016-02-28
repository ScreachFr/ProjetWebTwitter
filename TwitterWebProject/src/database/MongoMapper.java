package database;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


/**
 * @author marin
 * MongoMapper.java
 * MongoDB Connection.
 */
public class MongoMapper {
	private final static String HOST = "132.227.201.129";
	private final static int PORT = 27130;
	public final static String DATABASE = "gr3_dupas_gaspar";

	private final static String DATE_PATTERN = "HH:mm:ss dd/MM/YY";
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);


	public static MongoDatabase getMongoDBConnection() throws SQLException {

		MongoClient m = new MongoClient(HOST, PORT);
		MongoDatabase db = m.getDatabase(DATABASE);
		return db;
	}
	
	
	public static BasicDBObject executeQuery(String collection,QueryType type, Document doc) throws SQLException {
		MongoDatabase database = getMongoDBConnection();
		BasicDBObject result = null;
		MongoCollection<Document> collect = database.getCollection(collection);

		switch (type) {
		case SELECT:
			//TODO : remake how (Mongo) DBMapper is make : choice of the query is made with methods, not a string to send.
			break;
		case INSERT:
			collect.insertOne(doc);
			break;
		case DELETE:
			collect.deleteOne(doc);
			break;
		case UPDATE:
			break;
		}
		
		return result;

	}
	
	public static void executeInsertOne(String collection, Document doc) throws SQLException{
		MongoDatabase database = getMongoDBConnection();
		MongoCollection<Document> collect = database.getCollection(collection);
		collect.insertOne(doc);
	}
	
	public static void executeDeleteOne(String collection, Document doc) throws SQLException{
		MongoDatabase database = getMongoDBConnection();
		MongoCollection<Document> collect = database.getCollection(collection);
		collect.deleteOne(doc);
	}
	
	/**
	 * Execute a MongoDB query.
	 * @param collection
	 * 	Collection to use.
	 * @param whereArgs
	 * 	Arguments.
	 * @param startIndex
	 * 	Number of result to skip.
	 * @return
	 * 	Result.
	 * @throws SQLException
	 * XXX TEST : partial, not fully tested but for now it works. I guess.
	 */
	public static FindIterable<Document> executeGet(String collection, Map<String, Object> whereArgs, int startIndex) throws SQLException {
		MongoCollection<Document> database = getMongoDBConnection().getCollection(collection);
		BasicDBObject whereQuery = new BasicDBObject();
		FindIterable<Document> result;
		
		whereQuery.putAll(whereArgs);
		
		result = database.find(whereQuery);
		result.skip(startIndex);
		
		
		return result;
	}
	
	
	public enum QueryType {
		SELECT, UPDATE, DELETE, INSERT;
	}
	
	
	public static void main(String[] args) {
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("userid", 5);
			
			FindIterable<Document> result = executeGet("comments", params, 0);
			
			for (Document document : result) {
				System.out.println(document.get("content"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

