package services.errors;

public enum ServerErrors implements ServletError {
	MISSING_ARGUMENT(new GenericError(-1, "Missing argument(s)"));
	
	private GenericError error;
	
	private ServerErrors(GenericError error) {
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
