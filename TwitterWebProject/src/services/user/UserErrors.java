package services.user;

import services.errors.GenericError;
import services.errors.ServletError;

public enum UserErrors implements ServletError {
	LOGIN_ALREADY_EXIST(new GenericError(1, "Login already exist"));

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
