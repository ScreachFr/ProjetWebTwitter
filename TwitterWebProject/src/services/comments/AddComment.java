package services.comments;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.json.simple.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class AddComment extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6518914980624009051L;
	private static final String PARAM_TEXT = "text";
	private static final String PARAM_AUTHORLOGIN = "authorLogin";
	private static final String PARAM_USERID = "userId";
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		JSONObject answer;
		String userId;
		String authorLogin;
		String text;
		
		userId = req.getParameter(PARAM_USERID);
		authorLogin = req.getParameter(PARAM_AUTHORLOGIN);
		text = req.getParameter(PARAM_TEXT);
		
		
		answer = CommentsUtils.addComment(userId, authorLogin, text);
		
		PrintWriter out = resp.getWriter();
		out.write(answer.toJSONString());
		resp.setContentType("text/plain");
	}

	public static void main(String[] args) {
//		Mongo m = new Mongo("132.227.201.129", 27130); // Everything in comment is deprecated, so if the non deprecated work, no reason to keep the commented lines
		MongoClient m2 = new MongoClient("132.227.201.129", 27130);
		
//		DB db = m.getDB("gr3_dupas_gaspar");
		MongoDatabase db2 = m2.getDatabase("gr3_dupas_gaspar");
	
//		DBCollection comments = db.getCollection("comments");
		MongoCollection<Document> comments2 = db2.getCollection("comments");
	
//		BasicDBObject com = new BasicDBObject();
		Document com2 = new Document();

//		com.put("user_id", 1);
//		com.put("author_login", "1");
	
		GregorianCalendar calendar = new GregorianCalendar();
		Date dateAtM = calendar.getTime();
		
		com2.put("user_id", 1); //TODO : How rename documents from MongoDB
		com2.put("date", dateAtM);
		com2.put("author_login", "1");
		com2.put("text", "blabla");

		System.out.println("Test begin insert");
//		comments.insert(com);
		
		comments2.insertOne(com2);
		
		System.out.println("Test end insert");
		
		m2.close();
	}

}
