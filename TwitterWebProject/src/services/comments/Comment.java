package services.comments;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class Comment {

	private int authId;
	private String authLogin;
	private Date date;
	private String content;
	
	public Comment(int authId, String authLogin, Date date, String content) {
		super();
		this.authId = authId;
		this.authLogin = authLogin;
		this.date = date;
		this.content = content;
	}
	
	
	public int getAuthId() {
		return authId;
	}
	
	public String getAuthLogin() {
		return authLogin;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getContent() {
		return content;
	}

	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		
		
		result.put(CommentsUtils.USER_ID_MONGO, authId);
		result.put(CommentsUtils.AUTHOR_LOGIN_MONGO, authLogin);
		result.put(CommentsUtils.DATE_MONGO, date);
		result.put(CommentsUtils.CONTENT_MONGO, content);
		
		
		return result;
	}
	
	@Override
	public String toString() {
		return "Comment [authId=" + authId + ", authLogin=" + authLogin + ", date=" + date + ", content=" + content
				+ "]";
	}
	
	public static void main(String[] args) {
		Comment c = new Comment(1, "test", new Date(System.currentTimeMillis()), "Hello world!");
		
		System.out.println(c);
		System.out.println(c.toJSON());
	}
}
