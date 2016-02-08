package services.auth;

import org.json.simple.JSONObject;

import services.ServicesTools;
import services.errors.ServerErrors;

public class AuthenticationUtils {
	
	public static boolean checkLogin(String login) {
		return true;
	}
	
	public static boolean checkLoginAndPassword(String login, String password) {
		return true;
	}
	
	public static String addSession(int userId) {
		return "";
	}
	
	public static int getUserId(String login) {
		return 0;
	}
	
	public static boolean removeSession(int userId) {
		return true;
	}
	
	public static JSONObject generateLoginAnswer(String key) {
		JSONObject ret = new JSONObject();
		
		ret.put("key", key);
		
		return ret;
	}
	
	
	public static JSONObject generateLogoutAnswer() {
		JSONObject ret = new JSONObject();
		
		ret.put("success", true);
		
		return ret;
	}
	
	public static JSONObject login (String login, String password) {
		if (ServicesTools.nullChecker(login, password))
			return ServicesTools.createJSONError(ServerErrors.MISSING_ARGUMENT);
		else if (AuthenticationUtils.checkLoginAndPassword(login, password)) {
			String key;
			int userId;
			
			userId = AuthenticationUtils.getUserId(login);
			key = AuthenticationUtils.addSession(userId);
			return AuthenticationUtils.generateLoginAnswer(key);
		} else
			return ServicesTools.createJSONError(AuthErrors.BAD_LOGIN_OR_PASSWORD);
			
	}
	
	public static JSONObject logout (String login) {
		int userId;
		
		userId = AuthenticationUtils.getUserId(login);
		AuthenticationUtils.removeSession(userId);
		
		return AuthenticationUtils.generateLogoutAnswer();
	}
	
}
