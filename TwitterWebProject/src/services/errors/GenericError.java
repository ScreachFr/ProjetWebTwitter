package services.errors;


public class GenericError {
	private int code;
	private String message;
	
	public GenericError(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

}
