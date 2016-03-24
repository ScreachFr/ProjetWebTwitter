package services.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import services.ServicesTools;


public class ConnectionTest extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		JSONObject answer;
		
		ServicesTools.addCORSHeader(resp);
		
		answer = new JSONObject();
		
		answer.put("connection", "ok");
		
		
		PrintWriter out = resp.getWriter();
		out.write(answer.toJSONString());
		resp.setContentType("text/plain");
		
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		JSONObject answer;
		
		
		answer = new JSONObject();
		
		answer.put("connection", "ok");
		
		
		PrintWriter out = resp.getWriter();
		out.write(";" + answer.toJSONString());
		resp.setContentType("text/plain");
		
	}
	
}
