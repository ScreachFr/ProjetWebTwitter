package services.follower;

import services.errors.GenericError;
import services.errors.ServletError;

public enum FollowerErrors implements ServletError {
	UNKNOWN_USER(new GenericError(1, "Unknown user")),
	ALREADY_IN_LIST(new GenericError(2, "User already in friend list")),
	SAME_USER_ID(new GenericError(3, "You tried to follow yourself, it's kinda sad tbh")),
	ALREADY_FOLLOWING(new GenericError(4, "You already are following this user"));
	
	
	private GenericError error;
	
	private FollowerErrors(GenericError error) {
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
