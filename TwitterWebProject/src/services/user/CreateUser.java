package services.user;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import services.ServicesTools;

public class CreateUser extends HttpServlet {
	private static final long serialVersionUID = -3138477533724703698L;

	public final static String PARAM_LOGIN = "login";
	public final static String PARAM_PASSWORD = "password";
	public final static String PARAM_EMAIL = "email";
	public final static String PARAM_FNAME = "fname";
	public final static String PARAM_LNAME = "lname";
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		JSONObject answer;
		String login;
		String password;
		String email;
		String firstName;
		String lastName;;
		
		
		login = req.getParameter(PARAM_LOGIN);
		password = req.getParameter(PARAM_PASSWORD);
		email = req.getParameter(PARAM_EMAIL);
		firstName = req.getParameter(PARAM_FNAME);
		lastName = req.getParameter(PARAM_LNAME);
		
		ServicesTools.addCORSHeader(resp);
		
		answer = UserUtils.createUser(login, password, email, firstName, lastName);
		
		PrintWriter out = resp.getWriter();
		out.write(answer.toJSONString());
		resp.setContentType("text/plain");
	}

}
