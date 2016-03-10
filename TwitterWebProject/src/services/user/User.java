package services.user;

import org.json.simple.JSONObject;

public class User {
	private int id;
	private String login;
	private String email;
	private String firstName; //prenom
	private String lastName;

	public final static String ID			= "userId";
	public final static String LOGIN		= "login";
	public final static String EMAIL		= "email";
	public final static String FIRST_NAME	= "fName";
	public final static String LAST_NAME	= "lName";
	
	public User(int id, String login, String email, String firstName, String lastName) {
		this.id = id;
		this.login = login;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public int getId() {
		return id;
	}

	public String getLogin() {
		return login;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
	
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		ret.put(ID, id);
		ret.put(LOGIN, login);
		ret.put(EMAIL, email);
		ret.put(FIRST_NAME, firstName);
		ret.put(LAST_NAME, lastName);
		return ret;
	}
	
}
