package services.user;

import org.json.simple.JSONObject;

public class Stats {
	public final static String CLASS_JSON = "stats";
	public final static String FOLLOWS_JSON = "follows";
	public final static String FOLLOWERS_JSON = "followers";
	public final static String COMMENTS_JSON = "comments";
	
	private int follows;
	private int followers;
	private long comments;
	
	public Stats(int follows, int followers, long comments) {
		this.follows = follows;
		this.followers = followers;
		this.comments = comments;
	}
	
	public int getFollows() {
		return follows;
	}
	
	public int getFollowers() {
		return followers;
	}
	
	public long getComments() {
		return comments;
	}
	
	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		
		result.put(FOLLOWS_JSON, follows);
		result.put(FOLLOWERS_JSON, followers);
		result.put(COMMENTS_JSON, comments);
		
		return result;
	}
}
