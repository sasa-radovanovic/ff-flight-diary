package frequentFlyer.flight_diary.repos;

import frequentFlyer.flight_diary.Constants;
import frequentFlyer.flight_diary.models.Airport;
import frequentFlyer.flight_diary.models.statisticsModels.AirportStatistics;
import frequentFlyer.flight_diary.models.statisticsModels.BasicAirportInfo;
import frequentFlyer.flight_diary.models.statisticsModels.SingleAirportStatistics;
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
 * Airport Repo
 * 
 * This module does the actual database calls on airport table
 * 
 * @author Sasa Radovanovic
 *
 */
public class AirportRepo implements Repo {
	

	@Override
	public void getCount(JDBCClient jdbc, Handler<AsyncResult<Integer>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Could not open SQL connection"));
			}

			conn.query("SELECT count(*) FROM flight_diary_airports", query -> {
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

			conn.query("SELECT * FROM flight_diary_airports", query -> {
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
	 * @param code - IATA code of the airport to retrieve (e.g. BEG for Belgrade, LHR for London Heatrow, etc...)
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void getAirport (String iata_code, JDBCClient jdbc,
			Handler<AsyncResult<Airport>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}
			
			String sql = "SELECT * from flight_diary_airports where UPPER(iata_code)=?";
			
			conn.queryWithParams(sql, new JsonArray().add(iata_code), resultHandler -> {
				if (resultHandler.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(resultHandler.cause()));
				} else {
					conn.close(hand -> {
						Airport airport = new Airport(resultHandler.result().getRows().get(0));
						
						conn.close();
						
						handler.handle(Future.succeededFuture(airport));
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
	public void getPartialSearch (String criteria, JDBCClient jdbc,
			Handler<AsyncResult<JsonArray>> handler) {
		
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}
			
			String sql = "SELECT * from flight_diary_airports where UPPER(airport_name) like UPPER('%" + criteria + "%') or UPPER(airport_location) like UPPER('%" + criteria + "%') "
					+ "or UPPER(iata_code) like UPPER('%" + criteria + "%') or UPPER(icao_code) like UPPER('%" + criteria + "%') ORDER BY airport_name ASC LIMIT 10";
			
			conn.query(sql, resultHandler -> {
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
	 * 
	 * Retrieves all airport data
	 * 
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void getAirportStats(JDBCClient jdbc, Handler<AsyncResult<AirportStatistics>> handler) {
		
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}
			
			String sqlTotalUsedAirports = "select longitude, latitude, area_timezone from flight_diary_airports where iata_code in (select departure from flight_diary_flights) "
					+ "or iata_code in (select arrival from flight_diary_flights);";
			
			AirportStatistics airportStats = new AirportStatistics();
			
			conn.query(sqlTotalUsedAirports, resultHandler -> {
						if (resultHandler.failed()) {
							conn.close();
							handler.handle(null);
						} else {
							JsonArray arr = new JsonArray();
							resultHandler.result().getRows().forEach(arr::add);
							sortAirportsByHemipshere(airportStats, arr);
							
							String sqlMostFlownTo = "SELECT arrival, count(*) FROM flight_diary_flights GROUP BY arrival ORDER BY COUNT(*) DESC LIMIT 5";
							String sqlMostFlownFrom = "SELECT departure, count(*) FROM flight_diary_flights GROUP BY departure ORDER BY COUNT(*) DESC LIMIT 5";
							
							conn.query(sqlMostFlownTo, resultFlowTo -> {
								if (resultFlowTo.failed()) {
									conn.close();
									handler.handle(Future.succeededFuture(airportStats));
								} else {
									JsonArray arrTo = new JsonArray();
									resultFlowTo.result().getRows().forEach(arrTo::add);
									fillAirportsMostUsed(Constants.FLOWN_TO, airportStats, arrTo);
									conn.query(sqlMostFlownFrom, resultFlowFrom -> {
										if (resultFlowFrom.failed()) {
											conn.close();
											handler.handle(Future.succeededFuture(airportStats));
										} else {
											JsonArray arrFrom = new JsonArray();
											resultFlowFrom.result().getRows().forEach(arrFrom::add);
											fillAirportsMostUsed(Constants.FLOWN_FROM, airportStats, arrFrom);
											conn.close();
											handler.handle(Future.succeededFuture(airportStats));
										}
									});
								}
								
							});
						}
			});
		});
		
	}
	
	
	/**
	 * 
	 * Retrieve general statistics for the single airport
	 * 
	 * @param code - code of the airport to retrieve stats (e.g. BUD for Budapest)
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void getDetailedAirportStats(String iata_code, JDBCClient jdbc, Handler<AsyncResult<SingleAirportStatistics>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}
			
			SingleAirportStatistics singleAirportStats = new SingleAirportStatistics();
			
			String completeSql = "select flight_diary_users.username, flight_diary_flights.departure, flight_diary_flights.arrival, flight_diary_flights.rating, flight_diary_airports.longitude, flight_diary_airports.latitude from flight_diary_flights left join flight_diary_users on flight_diary_flights.user_id=flight_diary_users.id " + 
						"left join flight_diary_airports on (flight_diary_flights.departure=flight_diary_airports.iata_code and flight_diary_flights.departure not like '%" + iata_code + "%') or (flight_diary_flights.arrival=flight_diary_airports.iata_code and flight_diary_flights.arrival not like '%" + iata_code + "%') where flight_diary_flights.departure like '%" + iata_code + "%' or flight_diary_flights.arrival like '%" + iata_code + "%'";
		
			
			conn.query(completeSql, resultHandler -> {
				if (resultHandler.failed()) {
					handler.handle(Future.failedFuture(resultHandler.cause()));
					conn.close();
				} else {
					JsonArray resultArray = new JsonArray();
					resultHandler.result().getRows().forEach(resultArray::add);
					fillInSingleAirportStats(iata_code, singleAirportStats, resultArray);
					conn.close();
					handler.handle(Future.succeededFuture(singleAirportStats));
				}
			});
						
		});
		
	}
	
	
	/**
	 * 
	 * Helper method
	 * 
	 * @param iata_code - iata_code of the airport for filling
	 * @param singleAirportStats - SingleAirportStatistics object to fill
	 * @param resultArray - JsonArray to parse
	 */
	private void fillInSingleAirportStats(String iata_code,
			SingleAirportStatistics singleAirportStats, JsonArray resultArray) {
		double avgRating = 0;
		ArrayList<BasicAirportInfo> airportInfoList = new ArrayList<>();
		for (int i = 0; i < resultArray.size(); i++) {
			avgRating =+ resultArray.getJsonObject(i).getInteger("rating");
			String departure = resultArray.getJsonObject(i).getString("departure");
			String arrival = resultArray.getJsonObject(i).getString("arrival");
			if (departure.equalsIgnoreCase(iata_code)) {
				airportInfoList.add(new BasicAirportInfo(arrival, 
						resultArray.getJsonObject(i).getDouble("longitude"), resultArray.getJsonObject(i).getDouble("latitude")));
			} else {
				airportInfoList.add(new BasicAirportInfo(departure, 
						resultArray.getJsonObject(i).getDouble("longitude"), resultArray.getJsonObject(i).getDouble("latitude")));
			}
			singleAirportStats.increaseUsersActivityOnAirport(resultArray.getJsonObject(i).getString("username"));
			singleAirportStats.setConnectionAirports(airportInfoList);
		}
		singleAirportStats.setAverageRating(avgRating/resultArray.size());
		singleAirportStats.setVisited(resultArray.size());
	}

	/**
	 * 
	 * Helper method
	 * 
	 * @param airportStats - AirportStatistics object to fill
	 * @param resultArray - JsonArray to parse
	 */
	private void sortAirportsByHemipshere (AirportStatistics airportStats, JsonArray airportArray) {
		int northernAirports = 0;
		int southernAirports = 0;
		for (int i = 0; i < airportArray.size(); i++) {
			if (airportArray.getJsonObject(i).getDouble("longitude") >= 0) {
				northernAirports ++;
			} else {
				southernAirports ++;
			}
			airportStats.increaseRegion(airportArray.getJsonObject(i).getString("area_timezone").split("/")[0]);
		}
		airportStats.setNorthernAirports(northernAirports);
		airportStats.setSouthernAirports(southernAirports);
		airportStats.setTotalUsedAirports(airportArray.size());
	}
	
	/**
	 * 
	 * Helper method
	 * 
	 * @param type - Either flown to, or flown from
	 * @param airportStats - AirportStatistics object to fill
	 * @param resultArray - JsonArray to parse
	 */
	private void fillAirportsMostUsed (String type, AirportStatistics airportStats, JsonArray airportArray) {
		HashMap<String, Integer> localHashMap = new HashMap<>();
		if (type.equalsIgnoreCase(Constants.FLOWN_TO)) {
			for (int i = 0; i < airportArray.size(); i++) {
				localHashMap.put(airportArray.getJsonObject(i).getString("arrival"), airportArray.getJsonObject(i).getInteger("count"));
			}
			airportStats.setMostUsedArrival(localHashMap);
		} else if (type.equalsIgnoreCase(Constants.FLOWN_FROM)) {
			for (int i = 0; i < airportArray.size(); i++) {
				localHashMap.put(airportArray.getJsonObject(i).getString("departure"), airportArray.getJsonObject(i).getInteger("count"));
			}
			airportStats.setMostUsedDepartures(localHashMap);
		}
	}	

}
