package frequentFlyer.flight_diary;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;


/**
 * 
 * Database Connector
 * 
 * This module initializes Vertx JDBC connector with settings provided
 * 
 * @author Sasa Radovanovic
 *
 */
public class DatabaseConnector {
	
	private JDBCClient jdbc;
	
	// Singleton pattern
	private static DatabaseConnector databaseConnector = new DatabaseConnector();
	
	private DatabaseConnector() {	
	}
	
	public static DatabaseConnector getInstance( ) {
		return databaseConnector;
	}
	
	
	/**
	 * @param vertx - Vertx object to bind connector to
	 * @param url - URL of database adapter
	 * @param dbUser - username of database
	 * @param dbPassword - password for database
	 * @param driverClass - driver for database
	 * @param handler - Callback on job done
	 */
	public void setUpDatabase (Vertx vertx, String url, String dbUser, String dbPassword, String driverClass,  Handler<JDBCClient> handler) {
		if (jdbc == null) {
			jdbc = JDBCClient.createShared(vertx, new JsonObject()
			.put("url", url)
			.put("user", dbUser)
			.put("password", dbPassword)
			.put("driver_class", driverClass));
			handler.handle(jdbc);
		} else {
			handler.handle(null);
		}
	}
	
	/**
	 * @param handler - Return JDBC Connection
	 */
	public void getJDBCConnector (Handler<JDBCClient> handler) {
		if (jdbc == null) {
			handler.handle(null);
		} else {
			handler.handle(jdbc);
		}
	}
}
