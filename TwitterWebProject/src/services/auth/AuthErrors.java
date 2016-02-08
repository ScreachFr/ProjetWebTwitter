package services.auth;

import services.errors.GenericError;
import services.errors.ServletError;

public enum AuthErrors implements ServletError {
	BAD_LOGIN_OR_PASSWORD(new GenericError(1, "Bad login or password"));
	
	private GenericError error;
	
	private AuthErrors(GenericError error) {
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
