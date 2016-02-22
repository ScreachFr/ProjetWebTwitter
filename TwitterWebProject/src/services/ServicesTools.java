package services;

import org.json.simple.JSONObject;

import services.errors.ServletError;

public class ServicesTools {
	public static boolean nullChecker(Object... objs) {
		for (Object object : objs) {
			if (object == null)
				return true;
		}
		
		return false;
	}

	
	@SuppressWarnings("unchecked")
	public static JSONObject createJSONError(ServletError error) {
		JSONObject json = new JSONObject();
		json.put("errorCode", error.getCode());
		json.put("errorMessage", error.getMessage());
		
		return json;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject generatePositiveAnswer() {
		JSONObject ret = new JSONObject();

		ret.put("success", true);

		return ret;
	}
	
}
