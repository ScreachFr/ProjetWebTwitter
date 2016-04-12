package services.user;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import services.ServicesTools;
import services.errors.ServerErrors;

public class GetUser extends HttpServlet {
	private static final long serialVersionUID = -6499205360441859992L;

	private final static String PARAM_ID = "id";
	private final static String PARAM_YOUR_ID = "yourid";
	private final static String PARAM_INCLUDE_STATS = "includestats";


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		JSONObject answer;
		int id;
		int yourId;
		boolean includeStats;
		try {
			id = Integer.parseInt(req.getParameter(PARAM_ID));
			if(req.getParameter(PARAM_YOUR_ID) != null)
				yourId = Integer.parseInt(req.getParameter(PARAM_YOUR_ID));
			else
				yourId = -1;
			
			if(req.getParameter(PARAM_INCLUDE_STATS) != null)
				includeStats = Boolean.parseBoolean(req.getParameter(PARAM_INCLUDE_STATS));
			else
				includeStats = false;
			
			if(!ServicesTools.nullChecker(id)) {
				answer = UserUtils.getUserToJSON(id, yourId, includeStats);
			} else {
				answer = ServicesTools.createJSONError(ServerErrors.MISSING_ARGUMENT);
			}
		} catch (NumberFormatException e) {
			answer = ServicesTools.createJSONError(ServerErrors.BAD_ARGUMENT);
		}
		ServicesTools.addCORSHeader(resp);

		PrintWriter out = resp.getWriter();
		out.write(answer.toJSONString());
		resp.setContentType("text/plain");
	}

}
