package services.followers;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import services.ServicesTools;
import services.errors.ServerErrors;

public class Unfollow extends HttpServlet {
private static final long serialVersionUID = 7428447883130442228L;
	
	private final static String ID_TO_UNFOLLOW = "idtounfollow";
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		JSONObject answer;
		
		String key = null;
		Integer idToUnfollow = null;
		
		key = req.getParameter(ServicesTools.KEY_ARG);
		idToUnfollow = Integer.parseInt(req.getParameter(ID_TO_UNFOLLOW));

		if(!ServicesTools.nullChecker(key, idToUnfollow)) {
			answer = FollowerUtils.unfollow(key, idToUnfollow);
		} else {
			answer = ServicesTools.createJSONError(ServerErrors.MISSING_ARGUMENT);
		}
		
		
		
		PrintWriter out = resp.getWriter();
		out.write(answer.toJSONString());
		resp.setContentType("text/plain");
	}
}
