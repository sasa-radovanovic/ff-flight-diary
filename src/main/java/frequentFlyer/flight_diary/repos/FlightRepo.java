package frequentFlyer.flight_diary.repos;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import java.util.ArrayList;

import frequentFlyer.flight_diary.ApplicationCacheManager;
import frequentFlyer.flight_diary.Constants;
import frequentFlyer.flight_diary.mailer.ApplicationMailer;
import frequentFlyer.flight_diary.models.Flight;
import frequentFlyer.flight_diary.models.cacheModels.FlightData;
import frequentFlyer.flight_diary.models.complexModelBuilders.FlightExtended;
import frequentFlyer.flight_diary.models.statisticsModels.UserStatisics;



/**
 * 
 * Flight Repo
 * 
 * This module does the actual database calls on flight table
 * 
 * @author Sasa Radovanovic
 *
 */
public class FlightRepo implements Repo {


	private ApplicationCacheManager appCacheManager;
	private AirportRepo airportRepo;

	public FlightRepo (ApplicationMailer mailer) {
		this.appCacheManager = new ApplicationCacheManager(mailer, handler -> {
			System.out.println(this.getClass().getSimpleName() + " | Flight repo initialized with cache manager...");
			this.airportRepo = new AirportRepo();
		});
	}


	@Override
	public void getCount(JDBCClient jdbc, Handler<AsyncResult<Integer>> handler) {

		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Could not open SQL connection"));
			}

			conn.query("SELECT count(*) FROM flight_diary_flights", query -> {
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

			conn.query("SELECT * FROM flight_diary_flights", query -> {
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
	 * 
	 * Get all flights by specific user inc user data
	 * 
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void getAllRowsWithUserData(JDBCClient jdbc,
			Handler<AsyncResult<JsonArray>> handler) {

		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Could not open SQL connection"));
			}

			conn.query("SELECT * FROM flight_diary_flights INNER join flight_diary_users on (flight_diary_flights.user_id = flight_diary_users.id)", query -> {
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
	 * 
	 * Add a new flight
	 * 
	 * @param flight - Flight object to add to DB
	 * @param username - Username of user who adds a fligth
	 * @param mail - Mail of user
	 * @param id - Id of user who adds a flight
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void addFlight (Flight flight, String username, String mail, int id, JDBCClient jdbc, Handler<Future<FlightExtended>> handler) {


		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}

			String sql = "INSERT INTO flight_diary_flights (departure, arrival, departure_time, airline_code, rating, ticket_source, purpose, airplane_type, flight_class, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			conn.updateWithParams(sql,
					new JsonArray().add(flight.getDeparture()).add(flight.getArrival()).add(flight.getDeparture_time())
					.add(flight.getAirline_code()).add(flight.getRating()).add(flight.getTicket_source()).add(flight.getPurpose())
					.add(flight.getAirplane()).add(flight.getFlight_class()).add(id),
					(ar) -> {
						if (ar.failed()) {
							System.out.println("Failed " + ar.cause());
							handler.handle(Future.failedFuture(ar.cause()));
							conn.close();
							return;
						}
						UpdateResult result = ar.result();
						System.out.println("Added flight " + result.getKeys().getInteger(0));
						conn.queryWithParams("SELECT * FROM flight_diary_flights WHERE flight_id=?", new JsonArray().add(result.getKeys().getInteger(0)), insertedFlight -> {
							Flight createdFlight = new Flight(insertedFlight.result().getRows().get(0));
							System.out.println(createdFlight.toString());
							airportRepo.getAirport(createdFlight.getDeparture(), jdbc, departureAirport -> {
								if (departureAirport.succeeded()) {
									System.out.println(this.getClass().getSimpleName() + " | Received departure airport object");
									airportRepo.getAirport(createdFlight.getArrival(), jdbc, arrivalAirport -> {
										if (arrivalAirport.succeeded()) {
											System.out.println(this.getClass().getSimpleName() + " | Received arrival airport object");
											conn.close();
											handler.handle(Future.succeededFuture(new FlightExtended(createdFlight, 
													appCacheManager.getFlightDataOrCreate(departureAirport.result(), arrivalAirport.result(), username, mail, true))));
										}
									});
								}
							});

						});
					}
					);
		});

	}


	/**
	 * 
	 * Retrieve all users flights
	 *  
	 * @param username - Username of user for who to retrieve flights
	 * @param jdbc - JDBC connector to create SQL connections
	 * @param handler - Callback on job done
	 */
	public void getMyFlights (String username, JDBCClient jdbc, Handler<ArrayList<FlightExtended>> handler) {

		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}

			conn.query("SELECT * FROM flight_diary_flights where user_id in (select id from flight_diary_users where username='" + username + "' )", query -> {
				if (query.failed()) {
					conn.close();
					handler.handle(null);
				} else {
					conn.close(hand -> {
						ArrayList<FlightExtended> extendedList = new ArrayList<FlightExtended>();
						if (query.result().getRows().size() == 0) {
							handler.handle(extendedList);
						}
						for (JsonObject flightAsJson : query.result().getRows()) {
							Flight indivFlight = new Flight(flightAsJson);
							retrieveFlightDataFromCache(jdbc, indivFlight.getDeparture(), indivFlight.getArrival(), username, flightData -> {
								extendedList.add(new FlightExtended(indivFlight, flightData));
								if (extendedList.size() == query.result().getRows().size()) {
									handler.handle(extendedList);
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
	 * Delete a flight
	 * 
	 * @param flight_id - ID of flightto remove
	 * @param username - Username of user who removes the flight
	 * @param jdbc - JDBC connector for creating SQL connection
	 * @param handler - Callback on job done
	 */
	public void removeFlight (int flight_id, String username, JDBCClient jdbc, Handler<AsyncResult<Void>> handler) {

		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Could not open SQL connection"));
			}

			conn.query("DELETE from flight_diary_flights where flight_id=" + flight_id + " and user_id in (select id from flight_diary_users where username='" + username + "')", query -> {
				conn.close();
				System.out.println("Succedded");
				handler.handle(null);
			});
		});

	}

	/**
	 * 
	 * Edit a flight
	 * 
	 * @param flight - Flight to edit
	 * @param username - Username of user who edits the flight
	 * @param jdbc - JDBC connector for creating SQL connection
	 * @param handler - Callback on job done
	 */
	public void editFlight (Flight flight, String username, JDBCClient jdbc, Handler<AsyncResult<Void>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Could not open SQL connection"));
			}

			String queryStr = "UPDATE flight_diary_flights set airline_code='" + flight.getAirline_code() + 
					"' , departure='" + flight.getDeparture() + "' , arrival='" + flight.getArrival() + 
					"' , departure_time='" + flight.getDeparture_time() +  
					"' , airplane_type='"+ flight.getAirplane() + "' , rating=" + flight.getRating() + " , ticket_source='" + flight.getTicket_source() + "' , purpose=" + flight.getPurpose() + 
					", flight_class=" + flight.getFlight_class() + "  where flight_id=" + flight.getFlight_id() + " and user_id in (select id from flight_diary_users where username='" + username + "')";


			conn.query(queryStr, query -> {
				conn.close();
				handler.handle(null);
			});
		});

	}


	/**
	 * 
	 * Get stats for a single user based on flights
	 * 
	 * @param username - Username of user
	 * @param jdbc - JDBC connector for creating SQL connection
	 * @param handler - Callback on job done
	 */
	public void getUsersStats (JDBCClient jdbc, String username, Handler<AsyncResult<UserStatisics>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Could not open SQL connection"));
			}

			conn.query("SELECT * from flight_diary_flights where user_id in (select id from flight_diary_users where username='" + username + "' )", queryResult -> {

				UserStatisics userStats = new UserStatisics();
				
				for (JsonObject flightAsJson : queryResult.result().getRows()) {
					Flight indivFlight = new Flight(flightAsJson);
					String departureDate = indivFlight.getDeparture_time();
					fillDateInStats(userStats, departureDate);
				}
				conn.query("SELECT area_timezone from flight_diary_airports where iata_code in (select departure from flight_diary_flights where user_id in "
						+ "(select id from flight_diary_users where username='" + username + "')) "
								+ "or iata_code in (select arrival from flight_diary_flights where user_id in "
								+ "(select id from flight_diary_users where username='" + username + "'))", queryResultAirports -> {
					for (JsonObject timezone : queryResultAirports.result().getRows()) {
						String timezoneId = timezone.getString("area_timezone");
						userStats.increaseRegion(timezoneId.split("/")[0]);
					}
					conn.close();
					handler.handle(Future.succeededFuture(userStats));
				});
			});
		
		});
	}



	/**
	 * 
	 * Helper method
	 * 
	 * @param jdbc - JDBC connector to retrieve flight data
	 * @param codeAirportFrom - IATA code for departure airport
	 * @param codeAirportTo - IATA code for arrival airport
	 * @param username - username of user 
	 * @param handler - Callback on job done
	 */
	private void retrieveFlightDataFromCache (JDBCClient jdbc, String codeAirportFrom, String codeAirportTo, String username, Handler<FlightData> handler) {
		FlightData retData = appCacheManager.getFlightDataByCodes(codeAirportFrom, codeAirportTo);
		if (retData != null) {
			handler.handle(retData);
		}
		airportRepo.getAirport(codeAirportFrom, jdbc, departureAirport -> {
			if (departureAirport.succeeded()) {
				airportRepo.getAirport(codeAirportTo, jdbc, arrivalApt -> {
					if (arrivalApt.succeeded()) {
						handler.handle(appCacheManager.getFlightDataOrCreate(departureAirport.result(), arrivalApt.result(), username, "", false));
					}
				});
			}
		});
	}


	/**
	 * 
	 * Helper method
	 * 
	 * @param userStats - UserStatisics object to fill in the date
	 * @param dateToParse - date to parse and fill the stats with
	 */
	private void fillDateInStats (UserStatisics userStats, String dateToParse) {
		String[] dateTimeSplitted = dateToParse.split(" ");
		if (dateTimeSplitted.length != 2) {
			return;
		}
		String date = dateTimeSplitted[0];
		String time = dateTimeSplitted[1];
		String[] dateSeparated = date.split("/");
		if (dateSeparated.length != 3) {
			System.out.println("Date does not have three parts " + dateSeparated.length);
			return;
		}
		userStats.increaseMonth(reparseMonth(dateSeparated[1]));
		userStats.increaseYear(dateSeparated[2]);
		String[] timeSeparated = time.split(":");
		Integer hour = Integer.parseInt(timeSeparated[0]);
		if (hour < 12) {
			userStats.increaseTime(Constants.MORNING_FLIGHTS);
		} else if (hour < 18) {
			userStats.increaseTime(Constants.AFTERNOON_FLIGHTS);
		} else {
			userStats.increaseTime(Constants.EVENING_FLIGHTS);
		}

	}


	/**
	 * 
	 * Helper method for converting numbered string into human-readable month name
	 * @param month (e.g. "05")
	 * @return Name of the month (e.g. "May")
	 */
	private String reparseMonth (String month) {
		switch (month) {
		case "1" : return "January";
		case "01" : return "January";

		case "2" : return "February";
		case "02" : return "February";

		case "3" : return "March";
		case "03" : return "March";

		case "4" : return "April";
		case "04" : return "April";

		case "5" : return "May";
		case "05" : return "May";

		case "6" : return "June";
		case "06" : return "June";

		case "7" : return "July";
		case "07" : return "July";

		case "8" : return "August";
		case "08" : return "August";

		case "9" : return "September";
		case "09" : return "September";

		case "10" : return "October";
		case "11" : return "November";
		case "12" : return "December";		
		}
		return "";
	}


}
