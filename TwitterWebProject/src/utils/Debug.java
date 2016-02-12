package utils;

public class Debug {
	//If DEBUG is true the program will run in debug mode.
	public final static boolean DEBUG = true;
	
	/**
	 * Display a debug notice.
	 * @param notice
	 * 	String to display.
	 */
	public static void display_notice(String notice) {
		if(DEBUG)
			System.out.println("DEBUG : " + notice);
	}
	
	/**
	 * Display an exception's stack trace.
	 * @param e
	 * 	Exception to use for the stack trace's display.
	 */
	public static void display_stack(Exception e) {
		if(DEBUG) {
			System.out.println("DEBUG ------");
			e.printStackTrace();
			System.out.println("DEBUG ------");
		}
	}
	
}


