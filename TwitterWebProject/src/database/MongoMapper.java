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

	private static MongoDatabase db;
	

	public static MongoDatabase getMongoDBConnection() throws SQLException {

		MongoClient m = new MongoClient(HOST, PORT);
		
		if (db == null)
			db = m.getDatabase(DATABASE);
		
		return db;
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
	
	public static FindIterable<Document> executeGetWSort(String collection, Map<String, Object> whereArgs, Map<String, Object> sort, int startIndex) throws SQLException {
		MongoCollection<Document> database = getMongoDBConnection().getCollection(collection);
		BasicDBObject whereQuery = new BasicDBObject();
		BasicDBObject sortQuery = new BasicDBObject();
		FindIterable<Document> result;
		
		whereQuery.putAll(whereArgs);
		sortQuery.putAll(sort);
		
		result = database.find(whereQuery).sort(sortQuery);
		
		result.skip(startIndex);
		
		return result;
	}
	
	
	public enum Operator {
		GT("gt"), LT("lt");
		
		private String str;
		
		private Operator(String str) {
			this.str = str;
		}
		
		@Override
		public String toString() {
			return "$" + str;
		}
	}
}

