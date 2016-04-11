package services.search;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.simple.JSONObject;

import services.comments.Comment;
import services.comments.CommentsUtils;;

public class SearchResults {
	public final static String RESULTS_JSON = "results";
	public final static String QUERY_JSON = "query";
	
	private String query;
	private SortedSet<SearchResult> results;
	
	public SearchResults(String query) {
		results = new TreeSet<>();
		this.query = query;
	}
	
	public void addResult(String docId, double score) throws SQLException {
			Comment c = CommentsUtils.getComment(docId);
			
			addResult(c, score);
	}
	
	public void addResult(Comment c, double score) {
		results.add(new SearchResult(c, score));
	}
	
	public String getQuery() {
		return query;
	}
	
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		
		ret.put(QUERY_JSON, query);
		ret.put(RESULTS_JSON, results);
		
		return ret;
	}
	
}
