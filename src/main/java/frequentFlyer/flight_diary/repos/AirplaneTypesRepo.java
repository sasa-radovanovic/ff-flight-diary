package frequentFlyer.flight_diary.repos;

import frequentFlyer.flight_diary.models.AirplaneType;
import frequentFlyer.flight_diary.models.statisticsModels.SingleAirplaneTypeStatistics;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * 
 * Airplane Type Repo
 * 
 * This module does the actual database calls on airplane type table
 * 
 * @author Sasa Radovanovic
 *
 */
public class AirplaneTypesRepo implements Repo {

	@Override
	public void getCount(JDBCClient jdbc, Handler<AsyncResult<Integer>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Could not open SQL connection"));
			}

			conn.query("SELECT count(*) FROM flight_diary_airplane_types", query -> {
				if (query.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(query.cause()));
				} else {
					conn.close(hand -> {
						handler.handle(Future.succeededFuture(query.result().getResults().get(0).getInteger(0)));
					});
				}
			});
		});

	}

	@Override
	public void getAllRows(JDBCClient jdbc,
			Handler<AsyncResult<JsonArray>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Could not open SQL connection"));
			}

			conn.query("SELECT * FROM flight_diary_airplane_types", query -> {
				if (query.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(query.cause()));
				} else {
					conn.close(hand -> {
						JsonArray arr = new JsonArray();
						query.result().getRows().forEach(arr::add);
						conn.close();
						handler.handle(Future.succeededFuture(arr));
					});
				}
			});
		});

	}

	/**
	 * @param criteria - criteria text for partial search
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void getPartialSearch(String criteria, JDBCClient jdbc, Handler<AsyncResult<JsonArray>> handler) {

		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}

			String sql = "SELECT * from flight_diary_airplane_types where UPPER(type_code) like UPPER('%" + criteria + "%') or UPPER(type_name) like UPPER('%" + criteria + "%')"
					+ " ORDER BY type_code ASC LIMIT 10";

			conn.query(sql,resultHandler -> {
				if (resultHandler.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(resultHandler.cause()));
				} else {
					conn.close(hand -> {
						JsonArray arr = new JsonArray();
						resultHandler.result().getRows().forEach(arr::add);
						handler.handle(Future.succeededFuture(arr));
					});
				}
			});
		});
	}

	

	/**
	 * @param code - IATA code of the airplane type to retrieve (e.g. 320 for Airbus A320)
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void getAirplaneType(String code, JDBCClient jdbc, Handler<AsyncResult<AirplaneType>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}

			String sql = "SELECT * from flight_diary_airplane_types where UPPER(type_code)=?";

			conn.queryWithParams(sql, new JsonArray().add(code), resultHandler -> {
				if (resultHandler.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(resultHandler.cause()));
				} else {
					conn.close(hand -> {
						AirplaneType airplaneType = new AirplaneType(resultHandler.result().getRows().get(0));

						conn.close();

						handler.handle(Future.succeededFuture(airplaneType));
					});
				}
			});

		});

	}


	/**
	 * @param code - IATA code of the airplane type to retrieve stats (e.g. 788 for Boeing 787-8)
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void getAirplaneTypeStats(String code, JDBCClient jdbc, Handler<AsyncResult<SingleAirplaneTypeStatistics>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}

			String sql = "select flight_diary_users.username, flight_diary_flights.departure, "
					+ "flight_diary_flights.arrival, flight_diary_flights.rating from flight_diary_flights "
					+ "left join flight_diary_users on flight_diary_flights.user_id=flight_diary_users.id "
					+ "left join flight_diary_airplane_types on (flight_diary_flights.airplane_type=flight_diary_airplane_types.type_code) "
					+ "where flight_diary_flights.airplane_type like '%" + code + "%' GROUP BY flight_diary_users.username, "
					+ "flight_diary_flights.departure, flight_diary_flights.arrival, "
					+ "flight_diary_flights.rating;";

			SingleAirplaneTypeStatistics singleStats = new SingleAirplaneTypeStatistics();

			conn.query(sql, resultHandler -> {
				if (resultHandler.failed()) {
					conn.close();
					handler.handle(Future.succeededFuture(singleStats));
				} else {
					conn.close(hand -> {
						JsonArray resArray = new JsonArray();
						resultHandler.result().getRows().forEach(resArray::add);
						fillInAirplaneTypeStats(singleStats, resArray, fillHandle -> {
							handler.handle(Future.succeededFuture(singleStats));
						});
					});
				}
			});

		});

	}


	/**
	 * 
	 * Helper method
	 * 
	 * @param stats - SingleAirplaneTypeStatistics object to fill
	 * @param resultArray - JsonArray to parse and fill above 
	 * @param handler - Callback on job done
	 */
	private void fillInAirplaneTypeStats (SingleAirplaneTypeStatistics stats, JsonArray resultArray, Handler<Void> handler) {
		if (resultArray.size() > 0) {
			double avgRating = 0;
			HashMap<String, Integer> users = new HashMap<>();
			for (int i = 0; i < resultArray.size(); i++) {
				avgRating = avgRating + resultArray.getJsonObject(i).getInteger("rating");
				String departure = resultArray.getJsonObject(i).getString("departure");
				String arrival = resultArray.getJsonObject(i).getString("arrival");
				String user = resultArray.getJsonObject(i).getString("username");
				if (users.size() < 3 && !users.containsKey(user)) {
					users.put(user, 1);
				}
				stats.increaseAirport(departure);
				stats.increaseAirport(arrival);
			}
			avgRating = avgRating / resultArray.size();
			stats.setAverageRating(avgRating);
			ArrayList<String> usersList = new ArrayList<>();
			for (String uname : users.keySet()) {
				usersList.add(uname);
			}
			stats.setUsersFlown(usersList);
			stats.setTotalFlights(resultArray.size());
		}
		handler.handle(null);
	}

}
