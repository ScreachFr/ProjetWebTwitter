package services.auth;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

public class Logout extends HttpServlet {
	private static final long serialVersionUID = 1819978909505748438L;
	
	public final static String PARAM_LOGIN = "login";
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		JSONObject answer;
		String login;
		
		login = req.getParameter(PARAM_LOGIN);
		
		answer = AuthenticationUtils.logout(login);
		
		PrintWriter out = resp.getWriter();
		out.write(answer.toJSONString());
		resp.setContentType("text/plain");
		
	}
}
