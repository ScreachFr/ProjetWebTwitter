package services.search;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bson.Document;

import com.mongodb.client.MapReduceIterable;

import database.DBMapper;
import database.DBMapper.QueryType;
import database.MongoMapper;
import database.exceptions.CannotConnectToDatabaseException;
import database.exceptions.QueryFailedException;
import services.comments.CommentsUtils;
import utils.Debug;
import utils.FileLoader;

public class SearchUtils {
	private final static String BASE_PATH = "mapReduceFunctions/";

	private final static String DF_MAP_PATH = BASE_PATH + "mapDF.js";
	private final static String DF_REDUCE_PATH = BASE_PATH + "reduceDF.js";

	private final static String TF_MAP_PATH = BASE_PATH + "mapTF.js";
	private final static String TF_REDUCE_PATH = BASE_PATH + "reduceTF.js";

	private final static String TF_INSERT_QUERY = "INSERT INTO tfs values (?, ?, ?);";
	private final static String DF_INSERT_QUERY = "INSERT INTO idfs values (?, ?);";

	private final static String DF_UPDATE_QUERY = "UPDATE idfs SET df=? WHERE word=?;";

	public static void mapReduceComments() throws SQLException, IOException {
		MapReduceIterable<Document> tf, df;


		df = getDF();
		tf = getTF();

		updateTFSTable(tf);
		updateIDFSTable(df);


	}

	private static MapReduceIterable<Document> getDF() throws SQLException, IOException {
		MapReduceIterable<Document> mrResult;

		mrResult = MongoMapper.getMongoDBConnection().getCollection(CommentsUtils.COMMENT_COLLECTION_NAME)
				.mapReduce(FileLoader.loadFile(DF_MAP_PATH), FileLoader.loadFile(DF_REDUCE_PATH));


		return mrResult;
	}

	private static MapReduceIterable<Document> getTF() throws SQLException, IOException {
		MapReduceIterable<Document> mrResult;

		mrResult = MongoMapper.getMongoDBConnection().getCollection(CommentsUtils.COMMENT_COLLECTION_NAME)
				.mapReduce(FileLoader.loadFile(TF_MAP_PATH), FileLoader.loadFile(TF_REDUCE_PATH));


		return mrResult;
	}

	private static void updateTFSTable(MapReduceIterable<Document> tf) {
		String crtDocId;
		Document value;
		ArrayList<Document> values;
		Object v;

		try {
			for (Document doc : tf) {
				crtDocId = doc.get(CommentsUtils.COMMENT_ID_MONGO).toString();

				value = (Document) doc.get("value");

				v = value.get("values");

				if (v != null) {
					values = (ArrayList<Document>) v;

					for (Document document : values) {
						putTfToDB(crtDocId, document.getString("word"), document.getDouble("tf"));
					}

				} else {
					putTfToDB(crtDocId, value.getString("word"), value.getDouble("tf"));
				}
			}
		} catch(CannotConnectToDatabaseException e) {
			e.printStackTrace();
		} catch(QueryFailedException e) {
			e.printStackTrace();
		}

	}

	private static void putTfToDB(String docID, String word, double tf) throws CannotConnectToDatabaseException, QueryFailedException {
		try {
			DBMapper.executeQuery(TF_INSERT_QUERY, QueryType.INSERT, docID , word, tf);
		} catch (QueryFailedException e) {
			if(e.getSQLException().getErrorCode() != DBMapper.DUPLICATE_P_KEY_ERROR_CODE)
				throw e;
		}
	}


	private static void updateIDFSTable(MapReduceIterable<Document> df) {

		try {
			for (Document document : df) {
				putDfToDB(document.getString("_id"), document.getDouble("value"));
			}
		} catch (CannotConnectToDatabaseException e) {
			e.printStackTrace();
		} catch (QueryFailedException e) {
			e.printStackTrace();
		}
	}

	private static void putDfToDB(String word, double df) throws CannotConnectToDatabaseException, QueryFailedException {
		try {
			DBMapper.executeQuery(DF_INSERT_QUERY, QueryType.INSERT, word, df);
		} catch (QueryFailedException e) {
			if(e.getSQLException().getErrorCode() != DBMapper.DUPLICATE_P_KEY_ERROR_CODE)
				DBMapper.executeQuery(DF_UPDATE_QUERY, QueryType.UPDATE, df, word);
		}
	}


	public static void main(String[] args) {
		try {
			mapReduceComments();
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
