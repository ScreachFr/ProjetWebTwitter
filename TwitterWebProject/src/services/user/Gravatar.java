package services.user;

import services.ServicesTools;


public class Gravatar {
	private final static String BEFORE = "https://en.gravatar.com/avatar/";
	private final static String AFTER = ".jpg";
	private final static String SIZE = "&size=";
	private final static String DEFAULT = "?d=mm";
	
	public static String getGravatarUrl(String email) {
		String hash = ServicesTools.md5Hex(email); 
		
		return BEFORE + hash + AFTER + DEFAULT;
	}
	
	public static String getGravatarUrl(String email, int size) {
		return getGravatarUrl(email) + SIZE + size;
	}
	
	public static void main(String[] args) {
		System.out.println(getGravatarUrl("alexandregaspardcilia@hotmail.fr", 500));
	}
}


