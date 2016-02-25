package services.user;

public class User {
	private int id;
	private String login;
	private String email;
	private String firstName; //prenom
	private String lastName;
	
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
	
	
}
