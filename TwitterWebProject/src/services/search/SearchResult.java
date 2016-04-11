package services.search;

import org.json.simple.JSONObject;

import services.comments.Comment;

public class SearchResult implements Comparable<SearchResult>{
	public final static String COMMENT_JSON = "comment";
	public final static String SCORE_JSON = "score";

	
	private Comment comment;
	private double score;
	
	public SearchResult(Comment comment, double score) {
		this.comment = comment;
		this.score = score;
	}
	
	public Comment getComment() {
		return comment;
	}
	
	public double getScore() {
		return score;
	}
	
	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		
		result.put(COMMENT_JSON, comment.toJSON());
		result.put(SCORE_JSON, score);
		
		return result;
	}
	
	@Override
	public String toString() {
		return toJSON().toJSONString();
	}

	@Override
	public int compareTo(SearchResult o) {
		return Double.compare(o.getScore(), score);
	}
}
