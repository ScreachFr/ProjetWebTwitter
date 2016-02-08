package services.auth;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;


public class Login extends HttpServlet {
	private static final long serialVersionUID = 6278003606476939922L;

	public final static String PARAM_LOGIN = "login";
	public final static String PARAM_PASSWORD = "password";
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		JSONObject answer;
		String login;
		String password;
		
		login = req.getParameter(PARAM_LOGIN);
		password = req.getParameter(PARAM_PASSWORD);
		
		
		answer = AuthenticationUtils.login(login, password);
		
		PrintWriter out = resp.getWriter();
		out.write(answer.toJSONString());
		resp.setContentType("text/plain");
		
	}
	
}
