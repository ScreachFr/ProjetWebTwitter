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

import services.ServicesTools;

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
		String text;
		
		userId = req.getParameter(PARAM_USERID);
		text = req.getParameter(PARAM_TEXT);
		
		ServicesTools.addCORSHeader(resp);
		
		answer = CommentsUtils.addComment(userId, text);
		
		PrintWriter out = resp.getWriter();
		out.write(answer.toJSONString());
		resp.setContentType("text/plain");
	}


}
