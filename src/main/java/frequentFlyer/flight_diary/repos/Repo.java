package frequentFlyer.flight_diary.repos;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;

/**
 * 
 * Repo interface
 * 
 * Just an interface for design pattern
 * 
 * @author Sasa Radovanovic
 *
 */
public interface Repo {

	void getCount(JDBCClient jdbc, Handler<AsyncResult<Integer>> handler);
	
	void getAllRows (JDBCClient jdbc, Handler<AsyncResult<JsonArray>> handler);
	
}
