package services.search;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import database.MongoMapper.Operator;
import services.ServicesTools;
import services.errors.ServerErrors;

public class Search extends HttpServlet {
	private static final long serialVersionUID = 8349919187241077345L;

	private final static String PARAM_QUERY = "query";
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		JSONObject answer = null;
		ServicesTools.addCORSHeader(resp);
		
		String query = req.getParameter(PARAM_QUERY);
		
		if (ServicesTools.nullChecker(query)) {
			answer = ServicesTools.createJSONError(ServerErrors.MISSING_ARGUMENT);
		} else {
			answer = SearchUtils.search(query, -1);
		}
		
		PrintWriter out = resp.getWriter();
		out.write(answer.toJSONString());
		resp.setContentType("text/plain");
	}

}
