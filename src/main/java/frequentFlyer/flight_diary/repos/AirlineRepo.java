package frequentFlyer.flight_diary.repos;

import frequentFlyer.flight_diary.models.Airline;
import frequentFlyer.flight_diary.models.statisticsModels.BasicAirlineInfo;
import frequentFlyer.flight_diary.models.statisticsModels.BasicAirplaneTypeInfo;
import frequentFlyer.flight_diary.models.statisticsModels.BasicFlightInfo;
import frequentFlyer.flight_diary.models.statisticsModels.GeneralStatistics;
import frequentFlyer.flight_diary.models.statisticsModels.SingleAirlineStatistics;
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
 * Airline Repo
 * 
 * This module does the actual database calls on airline table
 * 
 * @author Sasa Radovanovic
 *
 */
public class AirlineRepo implements Repo {

	@Override
	public void getCount(JDBCClient jdbc, Handler<AsyncResult<Integer>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Could not open SQL connection"));
			}

			conn.query("SELECT count(*) FROM flight_diary_airlines", query -> {
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

			conn.query("SELECT * FROM flight_diary_airlines", query -> {
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

			String sql = "SELECT * from flight_diary_airlines where UPPER(airline_name) like UPPER('%" + criteria + "%') or UPPER(airline_iata_code) like UPPER('%" + criteria + "%') "
					+ "or UPPER(airline_icao_code) like UPPER('%" + criteria + "%') or UPPER(airline_country) like UPPER('%" + criteria + "%') ORDER BY airline_name ASC LIMIT 10";

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
	 * @param code - IATA code of the airline to retrieve
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void getAirline(String code, JDBCClient jdbc, Handler<AsyncResult<Airline>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}

			String sql = "SELECT * from flight_diary_airlines where UPPER(airline_iata_code)=?";

			conn.queryWithParams(sql, new JsonArray().add(code), resultHandler -> {
				if (resultHandler.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(resultHandler.cause()));
				} else {
					conn.close(hand -> {
						Airline airline = new Airline(resultHandler.result().getRows().get(0));

						conn.close();

						handler.handle(Future.succeededFuture(airline));
					});
				}
			});

		});

	}

	/**
	 * 
	 * Retrieve general statistics for the airlines and airline types
	 * 
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void getGeneralStatistics(JDBCClient jdbc, Handler<AsyncResult<GeneralStatistics>> handler) {

		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}

			String airlineSql = "SELECT flight_diary_airlines.airline_name, flight_diary_flights.airline_code, count(*) FROM flight_diary_flights INNER JOIN flight_diary_airlines ON flight_diary_flights.airline_code=flight_diary_airlines.airline_iata_code "
					+ "GROUP BY flight_diary_airlines.airline_name, flight_diary_flights.airline_code ORDER BY COUNT(*) DESC LIMIT 5";
			String airplaneTypesSql = "SELECT flight_diary_airplane_types.type_name, flight_diary_flights.airplane_type, count(*) FROM flight_diary_flights INNER JOIN flight_diary_airplane_types ON flight_diary_flights.airplane_type=flight_diary_airplane_types.type_code GROUP BY "
					+ "flight_diary_airplane_types.type_name, flight_diary_flights.airplane_type ORDER BY COUNT(*) DESC LIMIT 5";

			GeneralStatistics generalStats = new GeneralStatistics();

			conn.query(airlineSql, airlinesResultHandler -> {
				if (airlinesResultHandler.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(airlinesResultHandler.cause()));
				} else {
					JsonArray airlinesResultArray = new JsonArray();
					airlinesResultHandler.result().getRows().forEach(airlinesResultArray::add);
					fillGeneralStatsWithAirlines(generalStats, airlinesResultArray, airlineHandle -> {
						conn.query(airplaneTypesSql, airplaneTypesHandler -> {
							if (airplaneTypesHandler.failed()) {
								conn.close();
								handler.handle(Future.succeededFuture(generalStats));
							} else {
								conn.close(hand -> {
									JsonArray airplaneTypesResultArray = new JsonArray();
									airplaneTypesHandler.result().getRows().forEach(airplaneTypesResultArray::add);
									fillGeneralStatsWithAirplaneTypes(generalStats, airplaneTypesResultArray, airplaneTypeHandler -> {
										handler.handle(Future.succeededFuture(generalStats));
									});
								});
							}
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
	 * @param generalStats - GeneralStatistics object to fill
	 * @param resultArray - JsonArray to parse and fill above 
	 * @param handler - Callback on job done
	 */
	private void fillGeneralStatsWithAirlines (GeneralStatistics generalStats, JsonArray resultArray, Handler<Void> handler) {
		ArrayList<BasicAirlineInfo> airlineMap = new ArrayList<>();
		for (int i = 0; i < resultArray.size(); i++) {
			String airline_name = resultArray.getJsonObject(i).getString("airline_name");
			String airline_code = resultArray.getJsonObject(i).getString("airline_code");
			Integer count = resultArray.getJsonObject(i).getInteger("count");
			airlineMap.add(new BasicAirlineInfo(airline_name, airline_code, count));
		}
		generalStats.setAirlines(airlineMap);
		handler.handle(null);
	}

	/**
	 * 
	 * Helper method
	 * 
	 * @param generalStats - GeneralStatistics object to fill
	 * @param resultArray - JsonArray to parse and fill above 
	 * @param handler - Callback on job done
	 */
	private void fillGeneralStatsWithAirplaneTypes (GeneralStatistics generalStats, JsonArray resultArray, Handler<Void> handler) {
		ArrayList<BasicAirplaneTypeInfo> airplaneTypeMap = new ArrayList<>();
		for (int i = 0; i < resultArray.size(); i++) {
			String type_name = resultArray.getJsonObject(i).getString("type_name");
			String airplane_type = resultArray.getJsonObject(i).getString("airplane_type");
			Integer count = resultArray.getJsonObject(i).getInteger("count");
			airplaneTypeMap.add(new BasicAirplaneTypeInfo(type_name, airplane_type, count));
		}
		generalStats.setAirplaneTypes(airplaneTypeMap);
		handler.handle(null);
	}


	/**
	 * 
	 * Retrieve general statistics for the single airline
	 * 
	 * @param code - code of the airline to retrieve stats (e.g. EK for Emirates)
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void getSingleAirlineStats(String code, JDBCClient jdbc, Handler<AsyncResult<SingleAirlineStatistics>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}
			
			String query = "select flight_diary_users.username, flight_diary_flights.departure, "
					+ "flight_diary_flights.arrival, flight_diary_flights.rating, flight_diary_airports.longitude, "
					+ "flight_diary_airports.latitude from flight_diary_flights left join flight_diary_users "
					+ "on flight_diary_flights.user_id=flight_diary_users.id left join flight_diary_airports "
					+ "on (flight_diary_flights.departure=flight_diary_airports.iata_code or flight_diary_flights.arrival=flight_diary_airports.iata_code) "
					+ "where flight_diary_flights.airline_code='" + code + "'";
			
			SingleAirlineStatistics airlineStats = new SingleAirlineStatistics();
			
			conn.query(query, airlinesStatsHandler -> {
				if (airlinesStatsHandler.failed()) {
					conn.close();
					handler.handle(Future.succeededFuture(airlineStats));
				} else {
					JsonArray resArray = new JsonArray();
					airlinesStatsHandler.result().getRows().forEach(resArray::add);
					fillInListsAirlineStats(airlineStats, resArray, statsHandler -> {
						conn.close();
						handler.handle(Future.succeededFuture(airlineStats));
					});
				}
			});
			
		});
	}
	
	
	/**
	 * 
	 * Helper method
	 * 
	 * @param stats - SingleAirlineStatistics object to fill
	 * @param resultArray - JsonArray to parse and fill above 
	 * @param handler - Callback on job done
	 */
	private void fillInListsAirlineStats (SingleAirlineStatistics stats, JsonArray resultArray, Handler<Void> handler) {
		double avgRating = 0;
		HashMap<String, BasicFlightInfo> flightMap = new HashMap<>();
		for (int i = 0; i < resultArray.size(); i++) {
			avgRating = avgRating + resultArray.getJsonObject(i).getInteger("rating");
			String departure = resultArray.getJsonObject(i).getString("departure");
			String arrival = resultArray.getJsonObject(i).getString("arrival");
			double longitude = resultArray.getJsonObject(i).getDouble("longitude");
			double latitude = resultArray.getJsonObject(i).getDouble("latitude");
			if (flightMap.containsKey(departure + "-" + arrival)) {
				BasicFlightInfo bfi = flightMap.get(departure + "-" + arrival);
				if (bfi.getLongitude_two() == -1) {
					bfi.setLongitude_two(longitude);
					bfi.setLatitude_two(latitude);
					flightMap.replace(departure + "-" + arrival, bfi);
					String user = resultArray.getJsonObject(i).getString("username");
					stats.increaseUser(user);
				}
			} else {
				BasicFlightInfo bfi = new BasicFlightInfo();
				bfi.setLongitude_one(longitude);
				bfi.setLatitude_one(latitude);
				flightMap.put(departure + "-" + arrival, bfi);
			}
		}
		avgRating = avgRating / resultArray.size();
		stats.setAverageRating(avgRating);
		stats.setTotalFlights( resultArray.size() / 2);
		stats.setFlightsList(flightMap);
		handler.handle(null);
	}

}
