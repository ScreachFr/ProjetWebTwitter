package database;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
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
	private final static String DATABASE = "gr3_dupas_gaspar";

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
	 * Return current time. This method is in DBMapper to be sure it's use with database interactions. 
	 * The used pattern is "HH:mm:ss dd/MM/YY".
	 */
	public static String getTime() {
		return DATE_FORMAT.format(new Date(System.currentTimeMillis()));
	}

	/**
	 * Parse a string to a date. Caution : this method is made to parse date from data base who follow the "HH:mm:ss dd/MM/YY" pattern.
	 * @param s
	 * 	String to parse.
	 * @return
	 * 	Date from parsed string.
	 */
	public static Date parseDate(String s) {
		try {
			return DATE_FORMAT.parse(s);
		} catch (ParseException e) {
			System.err.println("ERROR : Failed to parse time.");
			return null;
		}
	}

	public enum QueryType {
		SELECT, UPDATE, DELETE, INSERT;
	}
}

