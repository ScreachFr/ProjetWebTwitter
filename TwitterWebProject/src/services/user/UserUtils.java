package services.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONObject;

import database.DBMapper;
import database.DBMapper.QueryType;
import services.ServicesTools;
import services.auth.AuthenticationUtils;
import services.errors.ServerErrors;

public class UserUtils {

	public static JSONObject createUser(String login, String password,
			String email) {
		if (ServicesTools.nullChecker(login, password))
			return ServicesTools.createJSONError(ServerErrors.MISSING_ARGUMENT);
		else
			try {
				if (AuthenticationUtils.checkLogin(login)) {
					return ServicesTools.createJSONError(UserErrors.LOGIN_ALREADY_EXIST);
				} else
					UserUtils.addUser(login, password, email);
					return null; //TODO
			} catch (SQLException e) {
				return ServicesTools.createJSONError(null);//TODO
			}
	}

	private static void addUser(String login, String password, String email) throws SQLException {
		ResultSet result = DBMapper.executeQuery("insert into users(login, password, email", QueryType.INSERT, login, password, email);
		boolean found = result.next();
		result.close(); //TODO
	}

}
