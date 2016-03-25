package services.user;

import services.errors.GenericError;
import services.errors.ServletError;

public enum UserErrors implements ServletError {
	LOGIN_OR_EMAIL_ALREADY_EXIST(new GenericError(1, "Login or email already exist")),
	UNKNOWN_USER(new GenericError(2, "Unknown user"));

	private GenericError error;
	
	private UserErrors(GenericError error) {
		this.error = error;
	}
	
	@Override
	public int getCode() {
		return error.getCode();
	}

	@Override
	public String getMessage() {
		return error.getMessage();
	}

}
