package services.search;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bson.Document;
import org.json.simple.JSONObject;

import com.mongodb.client.MapReduceIterable;

import database.DBMapper;
import database.DataBaseErrors;
import database.DBMapper.QueryType;
import database.MongoMapper;
import database.exceptions.CannotConnectToDatabaseException;
import database.exceptions.QueryFailedException;
import services.ServicesTools;
import services.comments.Comment;
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

	private final static String SEARCH_QUERY 		= "SELECT docid, (idfs.df + tfs.tf) as score FROM idfs INNER JOIN tfs ON (idfs.word = tfs.word) WHERE idfs.word IN (?) GROUP BY docid ORDER BY score DESC LIMIT 20;";
	private final static String GET_DF_QUERY 		= "SELECT df FROM idfs WHERE word = ?;";
	private final static String GET_TF_QUERY 		= "SELECT tf FROM tfs WHERE docid = ? AND word = ?;";
	private final static String GET_DOC_COUNT_QUERY = "SELECT count(DISTINCT docid) as nbdoc FROM gr3_dupas_gaspar.tfs;";

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

	private static ResultSet executeSearchQuery(String query) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		ResultSet result = DBMapper.executeQuery(SEARCH_QUERY, QueryType.SELECT, query);

		return result;
	}



	public static JSONObject search(String query, int iduser) {
		JSONObject answer;
		SearchResults result = new SearchResults(query);
		ResultSet searchQueryResult;
		String[] sQuery;
		Set<Comment> comments = new HashSet<>();

		try {
			sQuery = query.split(" ");

			for (String crtStr : sQuery) {
				searchQueryResult = executeSearchQuery(crtStr);

				while(searchQueryResult.next()) {
					comments.add(CommentsUtils.getComment(searchQueryResult.getString("docid")));
				}

				for (Comment comment : comments) {
					result.addResult(comment, rsv(comment.getCommentId(), query));
				}

			}

			answer = result.toJSON();

		} catch (SQLException e) {
			return ServicesTools.createJSONError(DataBaseErrors.UKNOWN_SQL_ERROR); 
		} catch (CannotConnectToDatabaseException e) {
			return ServicesTools.createJSONError(DataBaseErrors.CANNOT_CONNECT_TO_DATABASE);
		} catch (QueryFailedException e) {
			return ServicesTools.createJSONError(DataBaseErrors.QUERY_FAILED);
		}


		return answer;
	}

	private static double rsv(String docid, String query) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		double result = 0; 
		String[] aQuery = query.split(" ");
		double df;
		double tf;
		int nbDoc = docCount();

		for (String crtStr : aQuery) {
			df = getSingleDF(crtStr);
			tf = getSingleTF(docid, crtStr);

			result += tf * Math.log(nbDoc/df);
		}

		return result;
	}

	private static double getSingleDF(String word) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		ResultSet qResult;

		qResult = DBMapper.executeQuery(GET_DF_QUERY, QueryType.SELECT, word);

		if (qResult.next())
			return qResult.getDouble("df");
		else
			return 0;
	}

	private static double getSingleTF(String docid, String word) throws CannotConnectToDatabaseException, QueryFailedException, SQLException {
		ResultSet qResult;

		qResult = DBMapper.executeQuery(GET_TF_QUERY, QueryType.SELECT, docid, word);

		if (qResult.next())
			return qResult.getDouble("tf");
		else
			return 0;
	}

	private static int docCount() throws SQLException, CannotConnectToDatabaseException, QueryFailedException {
		ResultSet qResult;

		qResult = DBMapper.executeQuery(GET_DOC_COUNT_QUERY, QueryType.SELECT);

		if (qResult.next())
			return qResult.getInt(1);
		else
			return 0;
	}

	public static void main(String[] args) {
		
		System.out.println(search("test map reduce", -1));
	}
	

}
