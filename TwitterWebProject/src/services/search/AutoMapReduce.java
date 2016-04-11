package services.search;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class AutoMapReduce extends HttpServlet {
	private static final long serialVersionUID = 5951364301609909027L;

	private final static long SLEEP_TIME = 1000 * 60 * 60 * 3 ; //3 hours
	
	@Override
	public void init() throws ServletException {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					try {
						SearchUtils.mapReduceComments();
						
					} catch (SQLException e) {
					} catch (IOException e) {
					}
					
					try {
						Thread.sleep(SLEEP_TIME);
					} catch (InterruptedException e) {
						
					}
				}				
			}
		}).start();
		
		
	}
}
