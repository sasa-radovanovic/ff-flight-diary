package frequentFlyer.flight_diary.auth;

import io.vertx.core.Handler;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;


/**
 * 
 * Application Auth Provider
 * 
 * This module is layer around Vertx JDBC auth provider
 * 
 * @author Sasa Radovanovic
 *
 */
public class ApplicationAuthProvider {

	private static ApplicationAuthProvider applicationAuthProvider = new ApplicationAuthProvider();

	private JDBCAuth authProvider; 

	private ApplicationAuthProvider() {

	}
	
	public static ApplicationAuthProvider getInstance() {
		return applicationAuthProvider;
	}
	
	public void setJDBCAuth (JDBCClient jdbc, Handler<JDBCAuth> handler) {
		if (authProvider == null) {
			System.out.println(this.getClass().getSimpleName() + " | Setting application auth provider...");
			authProvider = JDBCAuth.create(jdbc);
			authProvider.setAuthenticationQuery("SELECT PASSWORD, PASSWORD_SALT FROM flight_diary_users WHERE ACTIVATED=true AND USERNAME= ?");
			handler.handle(authProvider);
		} else {
			handler.handle(authProvider);
		}
	}
	
	public void authenticate (Handler<Boolean> handler) {
		
	}

}
