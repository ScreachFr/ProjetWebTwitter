package services;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.simple.JSONObject;

import services.errors.ServletError;

public class ServicesTools {
	//Common args name.
	public final static String KEY_ARG			= "key";
	public final static String USER_ID_ARG		= "userid";
	public final static String NB_FOLLOWS_ARG	= "nbFollows";
	public final static String NB_FOLLOWERS_ARG	= "nbFollowers";
	public static final Object NB_MESSAGES_ARG	= "nbMessages";

	/**
	 * Check if there's a null in params.
	 * @param objs
	 * 	Objects to test.
	 * @return
	 * 	True = there is a null. False = no nulls.
	 */
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


	public static String hex(byte[] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i]
					& 0xFF) | 0x100).substring(1,3));        
		}
		return sb.toString();
	}

	public static String md5Hex(String message) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return hex (md.digest(message.getBytes("CP1252")));
		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
}
