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

public class Follow extends HttpServlet {
	private static final long serialVersionUID = 7428447883130442228L;
	
	private final static String ID_TO_FOLLOW = "idtofollow";
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		JSONObject answer;
		
		String key = null;
		Integer idTofollow = null;
		
		key = req.getParameter(ServicesTools.KEY_ARG);
		idTofollow = Integer.parseInt(req.getParameter(ID_TO_FOLLOW));

		if(!ServicesTools.nullChecker(key, idTofollow)) {
			answer = FollowerUtils.follow(key, idTofollow);
		} else {
			answer = ServicesTools.createJSONError(ServerErrors.MISSING_ARGUMENT);
		}
		
		ServicesTools.addCORSHeader(resp);
		
		PrintWriter out = resp.getWriter();
		out.write(answer.toJSONString());
		resp.setContentType("text/plain");
	}
}
