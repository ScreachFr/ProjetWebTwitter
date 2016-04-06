package services.comments;

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

public class GetComment extends HttpServlet {
	private static final long serialVersionUID = -9010174788134094008L;
	
	public final static String PARAM_DATE = "date";
	public final static String PARAM_OPERATOR = "op";
	public final static String PARAM_MAX_RESULT = "mresult";
	
	public final static String OP_AFTER = "a";
	public final static String OP_BEFORE = "b";
	
	public final static int DFT_MAX_RESULT = 10;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		JSONObject answer;
		String date;
		String op;
		int maxResult;
		
		date = req.getParameter(PARAM_DATE);
		op = req.getParameter(PARAM_OPERATOR);
		
		if(ServicesTools.nullChecker(req.getParameter(PARAM_MAX_RESULT))) {
			maxResult = DFT_MAX_RESULT;
		} else {
			try {
			maxResult = Integer.parseInt(req.getParameter(PARAM_MAX_RESULT));
			} catch(NumberFormatException e) {
				maxResult = DFT_MAX_RESULT;
			}
		}
		
		ServicesTools.addCORSHeader(resp);

		if (!ServicesTools.nullChecker(date, op)) {
			switch (op) {
			case "a":
				answer = CommentsUtils.getCommentsDependsOnTime(date, maxResult, Operator.GT);
				break;
			case "b":
				answer = CommentsUtils.getCommentsDependsOnTime(date, maxResult, Operator.LT);
				break;
			default:
				answer = ServicesTools.createJSONError(ServerErrors.BAD_ARGUMENT);
				break;
			}
		} else {
			answer = ServicesTools.createJSONError(ServerErrors.MISSING_ARGUMENT);
		}
		
		PrintWriter out = resp.getWriter();
		out.write(answer.toJSONString());
		resp.setContentType("text/plain");
	}

}
