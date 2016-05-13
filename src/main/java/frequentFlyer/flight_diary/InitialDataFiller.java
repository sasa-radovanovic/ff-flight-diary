package frequentFlyer.flight_diary;

import java.util.ArrayList;

import frequentFlyer.flight_diary.auth.ApplicationSHAEncoder;
import frequentFlyer.flight_diary.models.User;
import frequentFlyer.flight_diary.repos.UsersRepo;
import frequentFlyer.flight_diary.utils.AirlinesParser;
import frequentFlyer.flight_diary.utils.AirplaneTypesParser;
import frequentFlyer.flight_diary.utils.AirportsParser;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

/**
 * 
 * Initial data Filler
 * 
 * This module controls the deletation and creation of DB tables on startup
 * This might be one of the worst parts of the application so through the comments
 * 
 * @author Sasa Radovanovic
 *
 */
public class InitialDataFiller {

	// Singleton pattern
	private static InitialDataFiller initialDataFiller = new InitialDataFiller();

	private ArrayList<JsonObject> userList = new ArrayList<>();
	private ArrayList<JsonObject> flightList = new ArrayList<>();



	private InitialDataFiller() {
	}

	public static InitialDataFiller getInstance( ) {
		return initialDataFiller;
	}

	
	/**
	 * @param cleanDatabase - if set to true Flights and User tables will be completely dropped
	 * @param fillWithFixtures - if set to true Flights and User tables will be filled with fixtures defined bellow
	 * 
	 * NOTE: If you are starting application for the first time, i recommend leaving upper two set to true for demo purposes
	 * 
	 * @param airportsLocation - Location of airports csv file on your file system
	 * @param airlinesLocation - Location of airlines csv file on your file system
	 * @param airplaneTypesLocation - Location of airline types csv file on your file system
	 * @param jdbc - JDBC connection from which SQL connections are made
	 * @param handler - Callback for the end of operation
	 */
	public void prefillData (boolean cleanDatabase, boolean fillWithFixtures, String airportsLocation, String airlinesLocation, String airplaneTypesLocation,
			JDBCClient jdbc, Handler<Void> handler) {

		jdbc.getConnection(res -> {
			if (res.failed()) {
				throw new RuntimeException(res.cause());
			}
			final SQLConnection conn = res.result();
			if (conn == null) {
				System.err.println("Connection is null!");
				throw new RuntimeException(res.cause());
			}

			cleanTables(cleanDatabase, conn, cleanTables -> {
				createUsersTable (conn, usersTable -> {
					fillTestUsers (fillWithFixtures, conn, userFixtures -> {
						createAirportsTable(conn, airportTable -> {
							parseAirports(airportTable.intValue(), airportsLocation, jdbc, airportsInserted -> {
								createAirplaneTypesTable (conn, airplaneTypesTable -> {
									parseAirplaneTypes(airplaneTypesTable.intValue(), airplaneTypesLocation, jdbc, airplaneTypesInserted -> {
										createFlightsTable(conn, flightTable -> {
											createAirlinesTable(conn, airlinesTable -> {
												parseAirlines(airlinesTable.intValue(), airlinesLocation, jdbc, airlinesInserted -> {
													fillTestFlights(fillWithFixtures, jdbc, conn, done -> {
														conn.close(hand -> {
															handler.handle(null);
															System.out.println(this.getClass().getSimpleName() + " | Database set");
														});
													});
												});
											});
										});
									});
								});
							});
						});
					});
				});
			});
		});



	}


	/**
	 * @param shouldCleanTables - If set to true, flights and user tables are dropped
	 * @param conn - SQL connection to use for these operations
	 * @param handler - Callback on job done
	 */
	private void cleanTables (boolean shouldCleanTables, SQLConnection conn, Handler<Void> handler) {
		if (shouldCleanTables) { 
			conn.execute("DROP TABLE IF EXISTS flight_diary_flights CASCADE", dd -> {
				if (dd.failed()) {
					System.out.println("Drop failed...");
					conn.close(hand -> {
						throw new RuntimeException(dd.cause());
					});
				} else {
					System.out.println(this.getClass().getSimpleName() + " | Dropped flights table");
					conn.execute("DROP TABLE IF EXISTS flight_diary_users CASCADE", dd2 -> {
						if (dd2.failed()) {
							System.out.println("Drop failed...");
							conn.close(hand -> {
								throw new RuntimeException(dd2.cause());
							});
						} else {
							System.out.println(this.getClass().getSimpleName() + " | Dropped users table");
							handler.handle(null);
						}
					});
				}
			});
		} else {
			System.out.println(this.getClass().getSimpleName() + " | Should NOT clean tables");
			handler.handle(null);
		}

	}


	/**
	 * 
	 * Creating Flights table
	 * 
	 * @param conn - SQL connection to use for these operations
	 * @param handler - Callback on job done
	 */
	private void createFlightsTable (SQLConnection conn, Handler<Void> handler) {

		conn.execute("CREATE TABLE IF NOT EXISTS flight_diary_flights (flight_id SERIAL primary key, departure character varying(3) NOT NULL references flight_diary_airports(iata_code), "
				+ "arrival character varying(3) NOT NULL references flight_diary_airports(iata_code), "
				+ "departure_time character varying(255) NOT NULL,"
				+ "airline_code character varying(255) NOT NULL, rating integer, ticket_source character varying(255), "
				+ "airplane_type character varying(3) references flight_diary_airplane_types(type_code), purpose integer, "
				+ "flight_class integer, user_id serial references flight_diary_users(id))", ddl -> {

					if (ddl.failed()) {
						System.out.println("FAILED CREATION " + ddl.cause());
						conn.close(hand -> {
							throw new RuntimeException(ddl.cause());
						});
					}

					handler.handle(null);

				});

	}

	
	/**
	 * 
	 * Creating Airports table
	 * 
	 * @param conn - SQL connection to use for these operations
	 * @param handler - Callback on job done
	 */
	private void createAirportsTable (SQLConnection conn, Handler<Integer> handler) {
		conn.execute("CREATE TABLE IF NOT EXISTS flight_diary_airports (airport_name character varying(255) NOT NULL, "
				+ "airport_location character varying(255) NOT NULL, iata_code character varying(3) NOT NULL primary key, "
				+ "icao_code character varying(4), longitude decimal NOT NULL, latitude decimal NOT NULL, altitude decimal NOT NULL, "
				+ "utc_offset decimal, area_timezone character varying(255))", ddl -> {

					if (ddl.failed()) {
						System.out.println("FAILED CREATION " + ddl.cause());
						conn.close(hand -> {
							throw new RuntimeException(hand.cause());
						});
					}

					conn.query("SELECT count(*) FROM flight_diary_airports", resultHandler -> {
						if (resultHandler.succeeded()) {
							if (resultHandler.result().getResults().get(0).getInteger(0) > 0) {
								handler.handle(resultHandler.result().getResults().get(0).getInteger(0));
							} else {
								handler.handle(0);
							}
						} else {
							throw new RuntimeException(resultHandler.cause());
						}
					});

				});

	}

	/**
	 * 
	 * Creating Users table
	 * 
	 * @param conn - SQL connection to use for these operations
	 * @param handler - Callback on job done
	 */
	private void createUsersTable (SQLConnection conn, Handler<Void> handler) {

			conn.execute("CREATE TABLE IF NOT EXISTS flight_diary_users (id SERIAL primary key, mail character varying(255) NOT NULL, "
					+ "username character varying(255) NOT NULL UNIQUE, "
					+ "password character varying(255) NOT NULL, password_salt character varying(255) NOT NULL, "
					+ "activated boolean NOT NULL)", ddl -> {
						if (ddl.failed()) {
							conn.close(hand -> {
								throw new RuntimeException(ddl.cause());
							});
						} 

						if (ddl.succeeded()) {
							handler.handle(null);
						}
					});

	}

	/**
	 * 
	 * Creating Airlines table
	 * 
	 * @param conn - SQL connection to use for these operations
	 * @param handler - Callback on job done
	 */
	private void createAirlinesTable (SQLConnection conn, Handler<Integer> handler) {
		conn.execute("CREATE TABLE IF NOT EXISTS flight_diary_airlines (airline_id SERIAL primary key, airline_name character varying(255) NOT NULL, "
				+ "airline_iata_code character varying(3) NOT NULL, "
				+ "airline_icao_code character varying(4), airline_country character varying(255), airline_callsign character varying(255))", ddl -> {
					if (ddl.failed()) {
						conn.close(hand -> {
							throw new RuntimeException(ddl.cause());
						});
					} 

					if (ddl.succeeded()) {
						conn.query("SELECT count(*) FROM flight_diary_airlines", resultHandler -> {
							if (resultHandler.succeeded()) {
								if (resultHandler.result().getResults().get(0).getInteger(0) > 0) {
									handler.handle(resultHandler.result().getResults().get(0).getInteger(0));
								} else {
									handler.handle(0);
								}
							} else {
								throw new RuntimeException(resultHandler.cause());
							}
						});
					}
				});
	}

	/**
	 * 
	 * Creating Airline Types table
	 * 
	 * @param conn - SQL connection to use for these operations
	 * @param handler - Callback on job done
	 */
	private void createAirplaneTypesTable (SQLConnection conn, Handler<Integer> handler) {
		conn.execute("CREATE TABLE IF NOT EXISTS flight_diary_airplane_types (type_code character varying(3) primary key, "
				+ "type_name character varying(255) NOT NULL)", ddl -> {
					if (ddl.failed()) {
						conn.close(hand -> {
							throw new RuntimeException(ddl.cause());
						});
					} 

					if (ddl.succeeded()) {
						conn.query("SELECT count(*) FROM flight_diary_airplane_types", resultHandler -> {
							if (resultHandler.succeeded()) {
								if (resultHandler.result().getResults().get(0).getInteger(0) > 0) {
									handler.handle(resultHandler.result().getResults().get(0).getInteger(0));
								} else {
									handler.handle(0);
								}
							} else {
								throw new RuntimeException(resultHandler.cause());
							}
						});
					}
				});
	}


	/**
	 * @param airports - Number of airports currently in the database, if there aren't any - they will be loaded from CSV
	 * @param airportsLoc - Location of csv file
	 * @param jdbc - JDBC connection for SQL Connection
	 * @param handler - Callback on job done
	 */
	private void parseAirports(int airports, String airportsLoc, JDBCClient jdbc, Handler<Void> handler) {
		if (airports == 0) {
			AirportsParser.parseCSV(airportsLoc, jdbc, csvHandle -> {
				handler.handle(null);
			});
		} else {
			handler.handle(null);
		}
	}


	/**
	 * @param airlines - Number of airlines currently in the database, if there aren't any - they will be loaded from CSV
	 * @param airlinesLoc - Location of csv file
	 * @param jdbc - JDBC connection for SQL Connection
	 * @param handler - Callback on job done
	 */
	private void parseAirlines(int airlines, String airlinesLoc, JDBCClient jdbc, Handler<Void> handler) {
		System.out.println(this.getClass().getSimpleName() + " | Airlines in DB " + airlines);
		if (airlines == 0) {
			System.out.println(this.getClass().getSimpleName() + " | Insert airlines");
			AirlinesParser.parseCSV(airlinesLoc, jdbc, csvHandle -> {
				handler.handle(null);
			});
		} else {
			handler.handle(null);
		}
	}

	/**
	 * @param airplaneTypes - Number of airline types currently in the database, if there aren't any - they will be loaded from CSV
	 * @param airplaneTypesLoc - Location of csv file
	 * @param jdbc - JDBC connection for SQL Connection
	 * @param handler - Callback on job done
	 */
	private void parseAirplaneTypes (int airplaneTypes, String airplaneTypesLoc, JDBCClient jdbc, Handler<Void> handler) {
		System.out.println(this.getClass().getSimpleName() + " | Types in DB " + airplaneTypes);
		if (airplaneTypes == 0) {
			System.out.println(this.getClass().getSimpleName() + " | Insert types");
			AirplaneTypesParser.parseCSV(airplaneTypesLoc, jdbc, csvHandle -> {
				handler.handle(null);
			});
		} else {
			handler.handle(null);
		}
	}


	/**
	 * @param shouldFill - parameter for triggering fill operation
	 * @param conn - SQL connection to be used for operation
	 * @param handler - Callback on job done
	 */
	private void fillTestUsers(boolean shouldFill, SQLConnection conn, Handler<Void> handler) {
		if (shouldFill) {
			fillUsersList(userListHandler -> {
				fillUsersToDb(conn, usersToDBHandler -> {
					handler.handle(null);
				});
			});
		} else {
			System.out.println(this.getClass().getSimpleName() + " | Should NOT fill test fixtures");
			handler.handle(null);
		}
	}


	/**
	 * @param shouldFill - parameter for triggering fill operation
	 * @param jdbc - JDBC connector for creating additional connections
	 * @param conn - SQL connection to be used for operation
	 * @param handler - Callback on job done
	 */
	private void fillTestFlights(boolean shouldFill, JDBCClient jdbc, SQLConnection conn, Handler<Void> handler) {

		if (shouldFill) {
			
			// (1) This only shows how to insert flights for the test user, fixtures are inserted at (2) 
			UsersRepo userRepo = new UsersRepo(null);

			userRepo.getUserByUsername("test", jdbc, user -> {
				User testUser = user.result();
				conn.execute("INSERT INTO flight_diary_flights (departure, arrival, departure_time, airline_code, rating, ticket_source, airplane_type, purpose, flight_class, user_id) "
						+ "VALUES ('BEG', 'KEF', '10/04/2016 10:10', 'FI', 4, 'airline', '752', 0, 0, " + testUser.getId() + ")" +
						", ('KEF', 'YVR', '12/04/2016 20:10', 'FI', 4, 'airline', '753', 0, 0, " + testUser.getId() + ")" + 
						", ('YVR', 'HNL', '12/04/2016 20:10', 'UA', 4, 'airline', '772', 0, 0, " + testUser.getId() + ")" + 
						", ('HNL', 'MEX', '12/04/2016 20:10', 'DL', 4, 'airline', '764', 0, 0, " + testUser.getId() + ")" +
						", ('MEX', 'FOR', '12/04/2016 20:10', 'LA', 4, 'airline', '788', 0, 0, " + testUser.getId() + ")" +
						", ('FOR', 'REC', '12/04/2016 20:10', 'LA', 3, 'agent', '321', 0, 0, " + testUser.getId() + ")" +
						", ('REC', 'FOR', '12/04/2016 20:10', 'LA', 4, 'airline', '321', 0, 0, " + testUser.getId() + ")" +
						", ('FOR', 'LGW', '12/04/2016 20:10', 'DY', 5, 'agent', '789', 0, 0, " + testUser.getId() + ")" + 
						", ('LGW', 'BUD', '12/04/2016 20:10', 'DY', 3, 'airline', '738', 0, 0, " + testUser.getId() + ")" + 
						", ('BUD', 'BEG', '12/04/2016 20:10', 'JU', 3, 'airline', 'AT7', 0, 0, " + testUser.getId() + ")" +
						", ('BEG', 'INI', '12/04/2016 20:10', 'JU', 3, 'airline', 'AT7', 0, 0, " + testUser.getId() + ")" + 
						", ('INI', 'ZAG', '12/04/2016 20:10', 'JU', 3, 'airline', '733', 0, 0, " + testUser.getId() + ")" +
						", ('ZAG', 'BEG', '12/04/2016 20:10', 'JU', 3, 'airline', '320', 0, 0, " + testUser.getId() + ")" +
						", ('BEG', 'KVO', '12/04/2016 20:10', 'JU', 3, 'airline', 'AT7', 0, 0, " + testUser.getId() + ")" +
						", ('KVO', 'BEG', '12/04/2016 20:10', 'JU', 3, 'airline', 'AT7', 0, 0, " + testUser.getId() + ")" +
						", ('BEG', 'HAM', '12/04/2016 20:10', 'JU', 3, 'airline', '319', 0, 0, " + testUser.getId() + ")" +
						", ('HAM', 'MEX', '12/04/2016 20:10', 'LH', 3, 'airline', '333', 0, 0, " + testUser.getId() + ")", fixtures -> {
							if (fixtures.failed()) {
								throw new RuntimeException(fixtures.cause());
							}

							// (2) This calls functions which first fill the list of JSON objects with data and then fo through and execute statements
							// This is done this way to minimize SQL query time
							if (fixtures.succeeded()) {
								fillUsersFlights (flightsInLIst -> {
									fillFlightsToDB(jdbc, conn, flightsInDB -> {
										handler.handle(null);
									});
								});
							}
						});
			});
		} else {
			System.out.println(this.getClass().getSimpleName() + " | Should NOT fill flight fixtures");
			handler.handle(null);
		}
	}

	
	/**
	 * @param passwordToCompute - password to compute
	 * @return JsonObject containing encrypted password and salt for encryption
	 */
	private JsonObject computePasswordAndSalt (String passwordToCompute) {
		JsonObject json = new JsonObject();
		String salt = "";
		String password = "";
		try {
			salt = ApplicationSHAEncoder.getSalt();
			password = ApplicationSHAEncoder.computeHash(passwordToCompute, salt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		json.put("salt", salt);
		json.put("password", password);
		return json;
	}


	/**
	 * 
	 * Fills the user list with JsonObject which are done user when insertion is done
	 * 
	 * @param handler - Callback on job done
	 */
	private void fillUsersList (Handler<Void> handler) {

		JsonObject user1 = new JsonObject();
		user1.put("username", "test");
		JsonObject user1_pass = computePasswordAndSalt("test");
		user1.put("password", user1_pass.getString("password"));
		user1.put("salt", user1_pass.getString("salt"));
		user1.put("email", "test@test.com");
		userList.add(user1);

		JsonObject user2 = new JsonObject();
		user2.put("username", "andreas_ham");
		JsonObject user2_pass = computePasswordAndSalt("hamburg");
		user2.put("password", user2_pass.getString("password"));
		user2.put("salt", user2_pass.getString("salt"));
		user2.put("email", "andreas@t-com.de");
		userList.add(user2);

		JsonObject user3 = new JsonObject();
		user3.put("username", "laszlo.kiss");
		JsonObject user3_pass = computePasswordAndSalt("ferencvaros");
		user3.put("password", user3_pass.getString("password"));
		user3.put("salt", user3_pass.getString("salt"));
		user3.put("email", "laszlo.pesta@telecom.hu");
		userList.add(user3);

		JsonObject user4 = new JsonObject();
		user4.put("username", "danny_ceo");
		JsonObject user4_pass = computePasswordAndSalt("thechard");
		user4.put("password", user4_pass.getString("password"));
		user4.put("salt", user4_pass.getString("salt"));
		user4.put("email", "danny.woodbridge@my-company.eu");
		userList.add(user4);

		JsonObject user5 = new JsonObject();
		user5.put("username", "green_madness");
		JsonObject user5_pass = computePasswordAndSalt("alohanuiloha");
		user5.put("password", user5_pass.getString("password"));
		user5.put("salt", user5_pass.getString("salt"));
		user5.put("email", "martecho.sukhibo@pt-hawaii.us");
		userList.add(user5);


		JsonObject user6 = new JsonObject();
		user6.put("username", "rsasa");
		JsonObject user6_pass = computePasswordAndSalt("frequentflyer");
		user6.put("password", user6_pass.getString("password"));
		user6.put("salt", user6_pass.getString("salt"));
		user6.put("email", "sasa1kg@gmail.com");
		userList.add(user6);


		JsonObject user7 = new JsonObject();
		user7.put("username", "hideOsaka");
		JsonObject user7_pass = computePasswordAndSalt("kixitm");
		user7.put("password", user7_pass.getString("password"));
		user7.put("salt", user7_pass.getString("salt"));
		user7.put("email", "hidetoshi_nagahiro@gmail.com");
		userList.add(user7);

		JsonObject user8 = new JsonObject();
		user8.put("username", "johnny_acid");
		JsonObject user8_pass = computePasswordAndSalt("newyorkcitybaby");
		user8.put("password", user8_pass.getString("password"));
		user8.put("salt", user8_pass.getString("salt"));
		user8.put("email", "johnny_allaround@newyorkabc.us");
		userList.add(user8);

		JsonObject user9 = new JsonObject();
		user9.put("username", "augusto.jimenez");
		JsonObject user9_pass = computePasswordAndSalt("buenosairesboca");
		user9.put("password", user9_pass.getString("password"));
		user9.put("salt", user9_pass.getString("salt"));
		user9.put("email", "viva_laboca@bocaargentina.ar");
		userList.add(user9);

		JsonObject user10 = new JsonObject();
		user10.put("username", "maria_cpt");
		JsonObject user10_pass = computePasswordAndSalt("maria123456");
		user10.put("password", user10_pass.getString("password"));
		user10.put("salt", user10_pass.getString("salt"));
		user10.put("email", "maria.etumbo@capetowngreenmarket.sa");
		userList.add(user10);

		System.out.println(this.getClass().getSimpleName() + " | Filled the fixture list with " + this.userList.size() + " users");

		handler.handle(null);

	}

	/**
	 * @param jdbc - JDBC connector for creating additional SQL connections
	 * @param conn - SQL connection for executing query
	 * @param handler - Callback on job done
	 */
	private void fillFlightsToDB (JDBCClient jdbc, SQLConnection conn, Handler<Void> handler) {
		if (this.userList.size() > 0) {

			UsersRepo userRepo = new UsersRepo(null);

			for (JsonObject flightsObj : flightList) {
				String username = flightsObj.getString("username");
				JsonArray flightArray = flightsObj.getJsonArray("flights");
				userRepo.getUserByUsername(username, jdbc, user -> {
					User testUser = user.result();
					String sqlFlights = "INSERT INTO flight_diary_flights (departure, arrival, departure_time, airline_code, rating, ticket_source, airplane_type, purpose, flight_class, user_id) VALUES ";
					for (int j=0; j<flightArray.size(); j++) {
						JsonObject singleFlight = flightArray.getJsonObject(j);
						String departure = singleFlight.getString("departure");
						String arrival = singleFlight.getString("arrival");
						String departure_time = singleFlight.getString("departure_time");
						String airline_code = singleFlight.getString("airline_code");
						String airplane_type = singleFlight.getString("airplane_type");
						String ticket_source = singleFlight.getString("ticket_source");
						int rating = singleFlight.getInteger("rating");
						int flight_class = singleFlight.getInteger("flight_class");
						int purpose = singleFlight.getInteger("purpose");
						if (j == flightArray.size() - 1) {
							sqlFlights = sqlFlights + "('" + departure + "','" + arrival +  "','" + departure_time + "','" + airline_code  + "'," + rating + 
									",'" + ticket_source + "','" + airplane_type + "'," + flight_class + "," + purpose + "," + testUser.getId() + ")";
						} else {
							sqlFlights = sqlFlights + "('" + departure + "','" + arrival +  "','" + departure_time + "','" + airline_code  + "'," + rating + 
									",'" + ticket_source + "','" + airplane_type + "'," + flight_class + "," + purpose + "," + testUser.getId() + "),";
						}
					}

					final String finalSql = sqlFlights;

					jdbc.getConnection(sql-> {
						SQLConnection conn1 = sql.result();
						conn1.execute(finalSql, fixtures -> {
							if (fixtures.failed()) {
								throw new RuntimeException(fixtures.cause());
							}

							if (fixtures.succeeded()) {
								System.out.println(this.getClass().getSimpleName() + " | Inserted flights to DB for user <" + username + ">");
								handler.handle(null);
							}
						});
					});

				});
			}
		} else {
			System.out.println(this.getClass().getSimpleName() + " | No flight fixtures to fill");
			handler.handle(null);
		}
	}



	/**
	 * 
	 * Actual insertion of user fixtures in DB
	 * 
	 * @param conn - SQL connection to execute fixture insertion
	 * @param handler - Callback on job done
	 */
	private void fillUsersToDb(SQLConnection conn, Handler<Void> handler) {
		if (this.userList.size() > 0) {

			String sqlQuery = "INSERT INTO flight_diary_users (mail, username, password, password_salt, activated) VALUES ";

			for (int i=0; i< this.userList.size(); i++) {
				JsonObject userJson = this.userList.get(i);
				String email = userJson.getString("email");
				String username = userJson.getString("username");
				String password = userJson.getString("password");
				String salt = userJson.getString("salt");
				if (i == this.userList.size() - 1) {
					sqlQuery = sqlQuery + "('" + email + "','" + username + "','" + password + "','" + salt + "', true)";
				} else {
					sqlQuery = sqlQuery + "('" + email + "','" + username + "','" + password + "','" + salt + "', true),";
				}
			}


			conn.execute(sqlQuery, fixtures -> {
				if (fixtures.failed()) {
					throw new RuntimeException(fixtures.cause());
				}

				if (fixtures.succeeded()) {
					System.out.println(this.getClass().getSimpleName() + " | Inserted fixture users " + this.userList.size());
					handler.handle(null);
				}
			});

		} else {
			System.out.println(this.getClass().getSimpleName() + " | No user fixtures to fill");
			handler.handle(null);
		}
	}


	/**
	 * 
	 * There are way better ways to do this... 
	 * Fill list of flights which will be inserted to the DB
	 * 
	 * @param handler - Callback on job done
	 */
	private void fillUsersFlights (Handler<Void> handler) {

		JsonObject flightsUser1 = new JsonObject();
		flightsUser1.put("username", "andreas_ham");
		JsonArray flightArray = new JsonArray();

		JsonObject flight11 = new JsonObject();
		flight11.put("departure", "HAM");
		flight11.put("arrival", "FRA");
		flight11.put("departure_time", "10/01/2014 07:15");
		flight11.put("airline_code", "LH");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "FRA");
		flight11.put("arrival", "YVR");
		flight11.put("departure_time", "10/01/2014 10:15");
		flight11.put("airline_code", "LH");
		flight11.put("airplane_type", "333");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "YVR");
		flight11.put("arrival", "YYC");
		flight11.put("departure_time", "17/01/2014 14:20");
		flight11.put("airline_code", "AC");
		flight11.put("airplane_type", "772");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "YYC");
		flight11.put("arrival", "FRA");
		flight11.put("departure_time", "17/01/2014 20:45");
		flight11.put("airline_code", "AC");
		flight11.put("airplane_type", "767");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "FRA");
		flight11.put("arrival", "HAM");
		flight11.put("departure_time", "18/01/2014 10:35");
		flight11.put("airline_code", "LH");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HAM");
		flight11.put("arrival", "HEL");
		flight11.put("departure_time", "23/02/2014 15:55");
		flight11.put("airline_code", "AY");
		flight11.put("airplane_type", "E90");
		flight11.put("rating", 5);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HEL");
		flight11.put("arrival", "TMP");
		flight11.put("departure_time", "24/02/2014 18:55");
		flight11.put("airline_code", "AY");
		flight11.put("airplane_type", "AT7");
		flight11.put("rating", 3);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "TMP");
		flight11.put("arrival", "ARN");
		flight11.put("departure_time", "25/02/2014 21:55");
		flight11.put("airline_code", "SK");
		flight11.put("airplane_type", "717");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ARN");
		flight11.put("arrival", "HAM");
		flight11.put("departure_time", "24/02/2014 11:25");
		flight11.put("airline_code", "SK");
		flight11.put("airplane_type", "736");
		flight11.put("rating", 3);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HAM");
		flight11.put("arrival", "BCN");
		flight11.put("departure_time", "10/04/2014 12:00");
		flight11.put("airline_code", "IB");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BCN");
		flight11.put("arrival", "MAD");
		flight11.put("departure_time", "15/04/2014 10:05");
		flight11.put("airline_code", "IB");
		flight11.put("airplane_type", "CR9");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "MAD");
		flight11.put("arrival", "HAM");
		flight11.put("departure_time", "15/04/2014 14:35");
		flight11.put("airline_code", "IB");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HAM");
		flight11.put("arrival", "DBV");
		flight11.put("departure_time", "26/07/2014 19:35");
		flight11.put("airline_code", "OU");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DBV");
		flight11.put("arrival", "ZAG");
		flight11.put("departure_time", "3/08/2014 11:55");
		flight11.put("airline_code", "OU");
		flight11.put("airplane_type", "DH4");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ZAG");
		flight11.put("arrival", "HAM");
		flight11.put("departure_time", "3/08/2014 17:00");
		flight11.put("airline_code", "OU");
		flight11.put("airplane_type", "100");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "TXL");
		flight11.put("arrival", "MUC");
		flight11.put("departure_time", "11/09/2014 10:00");
		flight11.put("airline_code", "4U");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "NUE");
		flight11.put("arrival", "BRE");
		flight11.put("departure_time", "15/09/2014 08:15");
		flight11.put("airline_code", "FR");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "HAM");
		flight11.put("arrival", "CDG");
		flight11.put("departure_time", "26/02/2015 09:15");
		flight11.put("airline_code", "AF");
		flight11.put("airplane_type", "318");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "CDG");
		flight11.put("arrival", "PUJ");
		flight11.put("departure_time", "26/02/2015 23:15");
		flight11.put("airline_code", "AF");
		flight11.put("airplane_type", "77W");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "PUJ");
		flight11.put("arrival", "ORY");
		flight11.put("departure_time", "04/03/2015 18:25");
		flight11.put("airline_code", "AF");
		flight11.put("airplane_type", "333");
		flight11.put("rating", 5);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ORY");
		flight11.put("arrival", "LBC");
		flight11.put("departure_time", "05/03/2015 12:25");
		flight11.put("airline_code", "VY");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		System.out.println(this.getClass().getSimpleName() + " | Added " + flightArray.size() + " flights to array for user <andreas_ham>");

		flightsUser1.put("flights", flightArray);
		flightList.add(flightsUser1);

		flightsUser1 = new JsonObject();
		flightsUser1.put("username", "laszlo.kiss");
		flightArray = new JsonArray();

		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "LTN");
		flight11.put("departure_time", "10/01/2016 07:15");
		flight11.put("airline_code", "W6");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LCY");
		flight11.put("arrival", "RTM");
		flight11.put("departure_time", "12/01/2016 07:15");
		flight11.put("airline_code", "BA");
		flight11.put("airplane_type", "E95");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "RTM");
		flight11.put("arrival", "BUD");
		flight11.put("departure_time", "14/01/2016 07:15");
		flight11.put("airline_code", "HV");
		flight11.put("airplane_type", "739");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "PMO");
		flight11.put("departure_time", "14/03/2016 07:15");
		flight11.put("airline_code", "U2");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "CTA");
		flight11.put("arrival", "BUD");
		flight11.put("departure_time", "16/03/2016 18:15");
		flight11.put("airline_code", "W6");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "PEK");
		flight11.put("departure_time", "23/03/2016 18:15");
		flight11.put("airline_code", "CA");
		flight11.put("airplane_type", "333");
		flight11.put("rating", 5);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "PEK");
		flight11.put("arrival", "BKK");
		flight11.put("departure_time", "24/03/2016 20:15");
		flight11.put("airline_code", "CA");
		flight11.put("airplane_type", "737");
		flight11.put("rating", 3);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BKK");
		flight11.put("arrival", "DOH");
		flight11.put("departure_time", "11/04/2016 20:15");
		flight11.put("airline_code", "QR");
		flight11.put("airplane_type", "388");
		flight11.put("rating", 5);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DOH");
		flight11.put("arrival", "BUD");
		flight11.put("departure_time", "12/04/2016 05:15");
		flight11.put("airline_code", "QR");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 5);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "SVO");
		flight11.put("departure_time", "18/04/2016 05:15");
		flight11.put("airline_code", "SU");
		flight11.put("airplane_type", "SU1");
		flight11.put("rating", 5);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SVO");
		flight11.put("arrival", "BUD");
		flight11.put("departure_time", "20/04/2016 23:15");
		flight11.put("airline_code", "SU");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 5);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flightsUser1.put("flights", flightArray);
		flightList.add(flightsUser1);

		System.out.println(this.getClass().getSimpleName() + " | Added " + flightArray.size() + " flights to array for user <laszlo.kiss>");

		flightsUser1 = new JsonObject();
		flightsUser1.put("username", "danny_ceo");
		flightArray = new JsonArray();

		flight11 = new JsonObject();
		flight11.put("departure", "LHR");
		flight11.put("arrival", "DUB");
		flight11.put("departure_time", "10/10/2015 07:15");
		flight11.put("airline_code", "EI");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DUB");
		flight11.put("arrival", "MAN");
		flight11.put("departure_time", "12/10/2015 17:45");
		flight11.put("airline_code", "EI");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LHR");
		flight11.put("arrival", "CPT");
		flight11.put("departure_time", "11/11/2015 17:45");
		flight11.put("airline_code", "BA");
		flight11.put("airplane_type", "744");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "CPT");
		flight11.put("arrival", "JNB");
		flight11.put("departure_time", "19/11/2015 20:45");
		flight11.put("airline_code", "SA");
		flight11.put("airplane_type", "343");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "JNB");
		flight11.put("arrival", "LHR");
		flight11.put("departure_time", "20/11/2015 01:45");
		flight11.put("airline_code", "SA");
		flight11.put("airplane_type", "346");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LGW");
		flight11.put("arrival", "MEX");
		flight11.put("departure_time", "20/12/2015 01:45");
		flight11.put("airline_code", "BY");
		flight11.put("airplane_type", "788");
		flight11.put("rating", 3);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "CUN");
		flight11.put("arrival", "LGW");
		flight11.put("departure_time", "03/01/2016 22:45");
		flight11.put("airline_code", "AM");
		flight11.put("airplane_type", "789");
		flight11.put("rating", 5);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LHR");
		flight11.put("arrival", "MSQ");
		flight11.put("departure_time", "03/03/2016 22:45");
		flight11.put("airline_code", "B2");
		flight11.put("airplane_type", "CR9");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "MSQ");
		flight11.put("arrival", "WAW");
		flight11.put("departure_time", "06/03/2016 18:45");
		flight11.put("airline_code", "LO");
		flight11.put("airplane_type", "E90");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "WAW");
		flight11.put("arrival", "LHR");
		flight11.put("departure_time", "06/03/2016 18:45");
		flight11.put("airline_code", "LO");
		flight11.put("airplane_type", "E95");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LHR");
		flight11.put("arrival", "ORD");
		flight11.put("departure_time", "06/04/2016 18:45");
		flight11.put("airline_code", "AA");
		flight11.put("airplane_type", "763");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ORD");
		flight11.put("arrival", "MIA");
		flight11.put("departure_time", "10/04/2016 21:45");
		flight11.put("airline_code", "B6");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "MIA");
		flight11.put("arrival", "LHR");
		flight11.put("departure_time", "16/04/2016 23:15");
		flight11.put("airline_code", "BA");
		flight11.put("airplane_type", "388");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flightsUser1.put("flights", flightArray);
		flightList.add(flightsUser1);

		System.out.println(this.getClass().getSimpleName() + " | Added " + flightArray.size() + " flights to array for user <danny_ceo>");


		flightsUser1 = new JsonObject();
		flightsUser1.put("username", "augusto.jimenez");
		flightArray = new JsonArray();

		flight11 = new JsonObject();
		flight11.put("departure", "EZE");
		flight11.put("arrival", "SCL");
		flight11.put("departure_time", "10/10/2015 07:15");
		flight11.put("airline_code", "TM");
		flight11.put("airplane_type", "764");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SCL");
		flight11.put("arrival", "AKL");
		flight11.put("departure_time", "12/10/2015 07:15");
		flight11.put("airline_code", "LA");
		flight11.put("airplane_type", "789");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "AKL");
		flight11.put("arrival", "SYD");
		flight11.put("departure_time", "12/10/2015 20:00");
		flight11.put("airline_code", "LA");
		flight11.put("airplane_type", "789");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SYD");
		flight11.put("arrival", "SCL");
		flight11.put("departure_time", "20/10/2015 20:00");
		flight11.put("airline_code", "QA");
		flight11.put("airplane_type", "744");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SCL");
		flight11.put("arrival", "AEP");
		flight11.put("departure_time", "21/10/2015 18:00");
		flight11.put("airline_code", "LA");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "EZE");
		flight11.put("arrival", "FCO");
		flight11.put("departure_time", "21/12/2015 18:00");
		flight11.put("airline_code", "AZ");
		flight11.put("airplane_type", "772");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "FCO");
		flight11.put("arrival", "VIE");
		flight11.put("departure_time", "22/12/2015 18:00");
		flight11.put("airline_code", "AZ");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "VIE");
		flight11.put("arrival", "MXP");
		flight11.put("departure_time", "04/01/2016 18:00");
		flight11.put("airline_code", "OS");
		flight11.put("airplane_type", "100");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LIN");
		flight11.put("arrival", "FCO");
		flight11.put("departure_time", "04/01/2016 22:00");
		flight11.put("airline_code", "AZ");
		flight11.put("airplane_type", "E90");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "FCO");
		flight11.put("arrival", "EZE");
		flight11.put("departure_time", "05/01/2016 04:00");
		flight11.put("airline_code", "AZ");
		flight11.put("airplane_type", "332");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "AEP");
		flight11.put("arrival", "USH");
		flight11.put("departure_time", "12/01/2016 04:00");
		flight11.put("airline_code", "AR");
		flight11.put("airplane_type", "737");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "USH");
		flight11.put("arrival", "GRU");
		flight11.put("departure_time", "15/01/2016 04:25");
		flight11.put("airline_code", "G3");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "GRU");
		flight11.put("arrival", "EZE");
		flight11.put("departure_time", "16/01/2016 16:15");
		flight11.put("airline_code", "G3");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "EZE");
		flight11.put("arrival", "LIM");
		flight11.put("departure_time", "22/02/2016 16:15");
		flight11.put("airline_code", "AR");
		flight11.put("airplane_type", "332");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LIM");
		flight11.put("arrival", "GIG");
		flight11.put("departure_time", "26/02/2016 11:00");
		flight11.put("airline_code", "LA");
		flight11.put("airplane_type", "788");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SDU");
		flight11.put("arrival", "AEP");
		flight11.put("departure_time", "28/02/2016 14:10");
		flight11.put("airline_code", "AR");
		flight11.put("airplane_type", "E90");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "EZE");
		flight11.put("arrival", "EWR");
		flight11.put("departure_time", "28/03/2016 19:50");
		flight11.put("airline_code", "UA");
		flight11.put("airplane_type", "772");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "EWR");
		flight11.put("arrival", "EZE");
		flight11.put("departure_time", "04/04/2016 07:10");
		flight11.put("airline_code", "UA");
		flight11.put("airplane_type", "77W");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "EZE");
		flight11.put("arrival", "MEX");
		flight11.put("departure_time", "05/05/2016 10:10");
		flight11.put("airline_code", "AM");
		flight11.put("airplane_type", "773");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "MEX");
		flight11.put("arrival", "EZE");
		flight11.put("departure_time", "08/05/2016 10:25");
		flight11.put("airline_code", "AM");
		flight11.put("airplane_type", "788");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "EZE");
		flight11.put("arrival", "MIA");
		flight11.put("departure_time", "08/10/1999 10:25");
		flight11.put("airline_code", "AA");
		flight11.put("airplane_type", "D10");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "MIA");
		flight11.put("arrival", "EZE");
		flight11.put("departure_time", "22/10/1999 19:25");
		flight11.put("airline_code", "AA");
		flight11.put("airplane_type", "M11");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "EZE");
		flight11.put("arrival", "ATL");
		flight11.put("departure_time", "18/07/2002 21:25");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "310");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ATL");
		flight11.put("arrival", "LHR");
		flight11.put("departure_time", "19/07/2002 05:20");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "744");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LHR");
		flight11.put("arrival", "JFK");
		flight11.put("departure_time", "24/07/2002 15:05");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "763");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "JFK");
		flight11.put("arrival", "ATL");
		flight11.put("departure_time", "24/07/2002 23:00");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "752");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "JFK");
		flight11.put("arrival", "EZE");
		flight11.put("departure_time", "25/07/2002 06:00");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "764");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flightsUser1.put("flights", flightArray);
		flightList.add(flightsUser1);

		System.out.println(this.getClass().getSimpleName() + " | Added " + flightArray.size() + " flights to array for user <augusto.jimenez>");


		flightsUser1 = new JsonObject();
		flightsUser1.put("username", "hideOsaka");
		flightArray = new JsonArray();

		flight11 = new JsonObject();
		flight11.put("departure", "ITM");
		flight11.put("arrival", "ICN");
		flight11.put("departure_time", "10/01/1996 10:00");
		flight11.put("airline_code", "KE");
		flight11.put("airplane_type", "744");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ICN");
		flight11.put("arrival", "NRT");
		flight11.put("departure_time", "14/01/1996 10:00");
		flight11.put("airline_code", "KE");
		flight11.put("airplane_type", "764");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "KIX");
		flight11.put("arrival", "SIN");
		flight11.put("departure_time", "14/01/2001 12:00");
		flight11.put("airline_code", "SQ");
		flight11.put("airplane_type", "772");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SIN");
		flight11.put("arrival", "BKK");
		flight11.put("departure_time", "18/01/2001 12:00");
		flight11.put("airline_code", "TG");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BKK");
		flight11.put("arrival", "KIX");
		flight11.put("departure_time", "21/01/2001 12:00");
		flight11.put("airline_code", "TG");
		flight11.put("airplane_type", "M11");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "KIX");
		flight11.put("arrival", "NRT");
		flight11.put("departure_time", "21/01/2004 12:00");
		flight11.put("airline_code", "NH");
		flight11.put("airplane_type", "733");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HND");
		flight11.put("arrival", "ITM");
		flight11.put("departure_time", "21/01/2004 12:00");
		flight11.put("airline_code", "JL");
		flight11.put("airplane_type", "310");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "KIX");
		flight11.put("arrival", "FRA");
		flight11.put("departure_time", "21/01/2016 12:00");
		flight11.put("airline_code", "LH");
		flight11.put("airplane_type", "748");
		flight11.put("rating", 3);
		flight11.put("flight_class", 2);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "FRA");
		flight11.put("arrival", "LUX");
		flight11.put("departure_time", "21/01/2016 12:00");
		flight11.put("airline_code", "LG");
		flight11.put("airplane_type", "ER4");
		flight11.put("rating", 2);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LUX");
		flight11.put("arrival", "MUC");
		flight11.put("departure_time", "25/01/2016 12:00");
		flight11.put("airline_code", "LG");
		flight11.put("airplane_type", "DH4");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "MUC");
		flight11.put("arrival", "KIX");
		flight11.put("departure_time", "25/01/2016 19:00");
		flight11.put("airline_code", "LH");
		flight11.put("airplane_type", "346");
		flight11.put("rating", 4);
		flight11.put("flight_class", 2);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "KIX");
		flight11.put("arrival", "KUL");
		flight11.put("departure_time", "25/03/2016 23:00");
		flight11.put("airline_code", "AK");
		flight11.put("airplane_type", "333");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "KUL");
		flight11.put("arrival", "LGK");
		flight11.put("departure_time", "03/04/2016 10:00");
		flight11.put("airline_code", "AK");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "LGK");
		flight11.put("arrival", "DMK");
		flight11.put("departure_time", "10/04/2016 12:00");
		flight11.put("airline_code", "AK");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DMK");
		flight11.put("arrival", "SGN");
		flight11.put("departure_time", "12/04/2016 12:00");
		flight11.put("airline_code", "AK");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SGN");
		flight11.put("arrival", "NRT");
		flight11.put("departure_time", "12/04/2016 12:00");
		flight11.put("airline_code", "JL");
		flight11.put("airplane_type", "773");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ICN");
		flight11.put("arrival", "LAX");
		flight11.put("departure_time", "04/12/2014 20:00");
		flight11.put("airline_code", "KE");
		flight11.put("airplane_type", "748");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LAX");
		flight11.put("arrival", "NRT");
		flight11.put("departure_time", "12/12/2014 20:00");
		flight11.put("airline_code", "NH");
		flight11.put("airplane_type", "77W");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HND");
		flight11.put("arrival", "GMP");
		flight11.put("departure_time", "03/12/2014 20:00");
		flight11.put("airline_code", "KE");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);



		flight11 = new JsonObject();
		flight11.put("departure", "PVG");
		flight11.put("arrival", "KUL");
		flight11.put("departure_time", "03/08/2015 20:00");
		flight11.put("airline_code", "AK");
		flight11.put("airplane_type", "343");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "KUL");
		flight11.put("arrival", "PEK");
		flight11.put("departure_time", "09/08/2015 20:00");
		flight11.put("airline_code", "AK");
		flight11.put("airplane_type", "333");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "PEK");
		flight11.put("arrival", "KIX");
		flight11.put("departure_time", "09/08/2015 23:00");
		flight11.put("airline_code", "CZ");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "KIX");
		flight11.put("arrival", "PVG");
		flight11.put("departure_time", "02/08/2015 23:00");
		flight11.put("airline_code", "MU");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "KIX");
		flight11.put("arrival", "MEL");
		flight11.put("departure_time", "01/05/2016 07:00");
		flight11.put("airline_code", "QA");
		flight11.put("airplane_type", "744");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SYD");
		flight11.put("arrival", "NRT");
		flight11.put("departure_time", "10/05/2016 19:00");
		flight11.put("airline_code", "QA");
		flight11.put("airplane_type", "388");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flightsUser1.put("flights", flightArray);
		flightList.add(flightsUser1);

		System.out.println(this.getClass().getSimpleName() + " | Added " + flightArray.size() + " flights to array for user <hideOsaka>");

		flightsUser1 = new JsonObject();
		flightsUser1.put("username", "rsasa");
		flightArray = new JsonArray();

		flight11 = new JsonObject();
		flight11.put("departure", "TSR");
		flight11.put("arrival", "VLC");
		flight11.put("departure_time", "20/07/2013 18:00");
		flight11.put("airline_code", "W6");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "VLC");
		flight11.put("arrival", "TSR");
		flight11.put("departure_time", "30/07/2013 21:00");
		flight11.put("airline_code", "W6");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "SXF");
		flight11.put("departure_time", "07/09/2013 14:00");
		flight11.put("airline_code", "U2");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SXF");
		flight11.put("arrival", "AMS");
		flight11.put("departure_time", "10/09/2013 07:00");
		flight11.put("airline_code", "U2");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "EIN");
		flight11.put("arrival", "BEG");
		flight11.put("departure_time", "14/09/2013 16:00");
		flight11.put("airline_code", "W6");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "CDG");
		flight11.put("departure_time", "26/12/2013 16:00");
		flight11.put("airline_code", "U2");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "CDG");
		flight11.put("arrival", "BUD");
		flight11.put("departure_time", "02/01/2014 12:25");
		flight11.put("airline_code", "U2");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "VKO");
		flight11.put("departure_time", "10/10/2013 11:05");
		flight11.put("airline_code", "W6");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "VKO");
		flight11.put("arrival", "BUD");
		flight11.put("departure_time", "16/10/2013 16:05");
		flight11.put("airline_code", "W6");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BEG");
		flight11.put("arrival", "SAW");
		flight11.put("departure_time", "20/03/2014 11:55");
		flight11.put("airline_code", "PG");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SAW");
		flight11.put("arrival", "BEG");
		flight11.put("departure_time", "23/03/2014 10:30");
		flight11.put("airline_code", "PG");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BEG");
		flight11.put("arrival", "MXP");
		flight11.put("departure_time", "25/04/2014 15:45");
		flight11.put("airline_code", "U2");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "MXP");
		flight11.put("arrival", "HAM");
		flight11.put("departure_time", "26/04/2014 09:20");
		flight11.put("airline_code", "4U");
		flight11.put("airplane_type", "CR9");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HAM");
		flight11.put("arrival", "STR");
		flight11.put("departure_time", "30/04/2014 06:40");
		flight11.put("airline_code", "4U");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "STR");
		flight11.put("arrival", "LIS");
		flight11.put("departure_time", "30/04/2014 09:20");
		flight11.put("airline_code", "4U");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LIS");
		flight11.put("arrival", "MXP");
		flight11.put("departure_time", "03/05/2014 20:40");
		flight11.put("airline_code", "U2");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "MXP");
		flight11.put("arrival", "BEG");
		flight11.put("departure_time", "04/05/2014 14:00");
		flight11.put("airline_code", "U2");
		flight11.put("airplane_type", "319");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "VIE");
		flight11.put("arrival", "NCE");
		flight11.put("departure_time", "20/07/2014 18:30");
		flight11.put("airline_code", "HG");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "NCE");
		flight11.put("arrival", "VIE");
		flight11.put("departure_time", "30/07/2014 13:00");
		flight11.put("airline_code", "HG");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "TMP");
		flight11.put("departure_time", "23/08/2014 18:05");
		flight11.put("airline_code", "FR");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HEL");
		flight11.put("arrival", "OSL");
		flight11.put("departure_time", "26/08/2014 07:15");
		flight11.put("airline_code", "DY");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "OSL");
		flight11.put("arrival", "KEF");
		flight11.put("departure_time", "26/08/2014 10:40");
		flight11.put("airline_code", "DY");
		flight11.put("airplane_type", "733");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "KEF");
		flight11.put("arrival", "ARN");
		flight11.put("departure_time", "29/08/2014 07:35");
		flight11.put("airline_code", "FI");
		flight11.put("airplane_type", "752");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ARN");
		flight11.put("arrival", "BEG");
		flight11.put("departure_time", "30/08/2014 17:20");
		flight11.put("airline_code", "DY");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "KUT");
		flight11.put("departure_time", "30/12/2014 22:50");
		flight11.put("airline_code", "W6");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "TBS");
		flight11.put("arrival", "IST");
		flight11.put("departure_time", "03/01/2014 05:10");
		flight11.put("airline_code", "KK");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SAW");
		flight11.put("arrival", "BEG");
		flight11.put("departure_time", "04/01/2014 10:30");
		flight11.put("airline_code", "PG");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "DXB");
		flight11.put("departure_time", "07/02/2015 15:05");
		flight11.put("airline_code", "EK");
		flight11.put("airplane_type", "332");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DXB");
		flight11.put("arrival", "BKK");
		flight11.put("departure_time", "09/02/2015 03:05");
		flight11.put("airline_code", "EK");
		flight11.put("airplane_type", "388");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DMK");
		flight11.put("arrival", "NRT");
		flight11.put("departure_time", "11/02/2015 23:45");
		flight11.put("airline_code", "AK");
		flight11.put("airplane_type", "333");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "NRT");
		flight11.put("arrival", "DXB");
		flight11.put("departure_time", "14/02/2015 22:00");
		flight11.put("airline_code", "EK");
		flight11.put("airplane_type", "77W");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DXB");
		flight11.put("arrival", "BUD");
		flight11.put("departure_time", "15/02/2015 08:20");
		flight11.put("airline_code", "EK");
		flight11.put("airplane_type", "332");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "TLV");
		flight11.put("departure_time", "15/05/2015 10:20");
		flight11.put("airline_code", "LY");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "TLV");
		flight11.put("arrival", "BUD");
		flight11.put("departure_time", "20/05/2015 21:45");
		flight11.put("airline_code", "W6");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "CRL");
		flight11.put("departure_time", "08/08/2015 18:45");
		flight11.put("airline_code", "FR");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BRU");
		flight11.put("arrival", "LIS");
		flight11.put("departure_time", "09/08/2015 15:55");
		flight11.put("airline_code", "FR");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LIS");
		flight11.put("arrival", "RAI");
		flight11.put("departure_time", "10/08/2015 16:10");
		flight11.put("airline_code", "VR");
		flight11.put("airplane_type", "752");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "RAI");
		flight11.put("arrival", "VXE");
		flight11.put("departure_time", "10/08/2015 19:30");
		flight11.put("airline_code", "VR");
		flight11.put("airplane_type", "AT7");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "VXE");
		flight11.put("arrival", "RAI");
		flight11.put("departure_time", "14/08/2015 12:20");
		flight11.put("airline_code", "VR");
		flight11.put("airplane_type", "AT7");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "RAI");
		flight11.put("arrival", "REC");
		flight11.put("departure_time", "14/08/2015 20:05");
		flight11.put("airline_code", "VR");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "REC");
		flight11.put("arrival", "GIG");
		flight11.put("departure_time", "15/08/2015 03:35");
		flight11.put("airline_code", "G3");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "GIG");
		flight11.put("arrival", "EZE");
		flight11.put("departure_time", "15/08/2015 09:21");
		flight11.put("airline_code", "G3");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 5);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "EZE");
		flight11.put("arrival", "GRU");
		flight11.put("departure_time", "18/08/2015 06:45");
		flight11.put("airline_code", "G3");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "GRU");
		flight11.put("arrival", "SDU");
		flight11.put("departure_time", "18/08/2015 11:30");
		flight11.put("airline_code", "G3");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "GIG");
		flight11.put("arrival", "BSB");
		flight11.put("departure_time", "21/08/2015 09:16");
		flight11.put("airline_code", "O6");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BSB");
		flight11.put("arrival", "FOR");
		flight11.put("departure_time", "21/08/2015 11:56");
		flight11.put("airline_code", "O6");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "FOR");
		flight11.put("arrival", "RAI");
		flight11.put("departure_time", "21/08/2015 23:59");
		flight11.put("airline_code", "VR");
		flight11.put("airplane_type", "752");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "RAI");
		flight11.put("arrival", "LIS");
		flight11.put("departure_time", "22/08/2015 08:10");
		flight11.put("airline_code", "VR");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LIS");
		flight11.put("arrival", "MAD");
		flight11.put("departure_time", "22/08/2015 16:10");
		flight11.put("airline_code", "TP");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "MAD");
		flight11.put("arrival", "BUD");
		flight11.put("departure_time", "23/08/2015 06:30");
		flight11.put("airline_code", "FR");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "BEG");
		flight11.put("arrival", "SVO");
		flight11.put("departure_time", "09/10/2015 00:50");
		flight11.put("airline_code", "SU");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SVO");
		flight11.put("arrival", "LED");
		flight11.put("departure_time", "09/10/2015 06:05");
		flight11.put("airline_code", "SU");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LED");
		flight11.put("arrival", "SVO");
		flight11.put("departure_time", "12/10/2015 03:50");
		flight11.put("airline_code", "SU");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SVO");
		flight11.put("arrival", "BEG");
		flight11.put("departure_time", "12/10/2015 10:25");
		flight11.put("airline_code", "SU");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "BUD");
		flight11.put("arrival", "DXB");
		flight11.put("departure_time", "18/05/2016 16:00");
		flight11.put("airline_code", "EK");
		flight11.put("airplane_type", "773");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DXB");
		flight11.put("arrival", "MLE");
		flight11.put("departure_time", "19/05/2016 02:45");
		flight11.put("airline_code", "EK");
		flight11.put("airplane_type", "77W");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "MLE");
		flight11.put("arrival", "SIN");
		flight11.put("departure_time", "23/05/2016 21:50");
		flight11.put("airline_code", "TR");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SIN");
		flight11.put("arrival", "KUL");
		flight11.put("departure_time", "24/05/2016 10:30");
		flight11.put("airline_code", "3K");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "KUL");
		flight11.put("arrival", "HKG");
		flight11.put("departure_time", "26/05/2016 13:15");
		flight11.put("airline_code", "AK");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HKG");
		flight11.put("arrival", "DXB");
		flight11.put("departure_time", "29/05/2016 00:35");
		flight11.put("airline_code", "EK");
		flight11.put("airplane_type", "388");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DXB");
		flight11.put("arrival", "BUD");
		flight11.put("departure_time", "29/05/2016 08:25");
		flight11.put("airline_code", "EK");
		flight11.put("airplane_type", "773");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flightsUser1.put("flights", flightArray);
		flightList.add(flightsUser1);

		System.out.println(this.getClass().getSimpleName() + " | Added " + flightArray.size() + " flights to array for user <rsasa>");

		flightsUser1 = new JsonObject();
		flightsUser1.put("username", "maria_cpt");
		flightArray = new JsonArray();

		flight11 = new JsonObject();
		flight11.put("departure", "CPT");
		flight11.put("arrival", "SIN");
		flight11.put("departure_time", "10/01/2006 07:15");
		flight11.put("airline_code", "SQ");
		flight11.put("airplane_type", "744");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SIN");
		flight11.put("arrival", "MEL");
		flight11.put("departure_time", "10/01/2006 18:00");
		flight11.put("airline_code", "SQ");
		flight11.put("airplane_type", "773");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "MEL");
		flight11.put("arrival", "SIN");
		flight11.put("departure_time", "18/01/2006 15:00");
		flight11.put("airline_code", "SQ");
		flight11.put("airplane_type", "332");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SIN");
		flight11.put("arrival", "CPT");
		flight11.put("departure_time", "18/01/2006 23:00");
		flight11.put("airline_code", "SQ");
		flight11.put("airplane_type", "744");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "CPT");
		flight11.put("arrival", "JNB");
		flight11.put("departure_time", "10/04/2006 10:00");
		flight11.put("airline_code", "SA");
		flight11.put("airplane_type", "733");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "JNB");
		flight11.put("arrival", "CPT");
		flight11.put("departure_time", "12/04/2006 18:00");
		flight11.put("airline_code", "SA");
		flight11.put("airplane_type", "734");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "CPT");
		flight11.put("arrival", "DUR");
		flight11.put("departure_time", "14/07/2006 08:40");
		flight11.put("airline_code", "SA");
		flight11.put("airplane_type", "735");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DUR");
		flight11.put("arrival", "PLZ");
		flight11.put("departure_time", "16/07/2006 11:00");
		flight11.put("airline_code", "SA");
		flight11.put("airplane_type", "CR2");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "PLZ");
		flight11.put("arrival", "CPT");
		flight11.put("departure_time", "16/07/2006 11:00");
		flight11.put("airline_code", "SA");
		flight11.put("airplane_type", "332");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 2);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "CPT");
		flight11.put("arrival", "AMS");
		flight11.put("departure_time", "10/10/2006 20:00");
		flight11.put("airline_code", "KL");
		flight11.put("airplane_type", "772");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "AMS");
		flight11.put("arrival", "GVA");
		flight11.put("departure_time", "11/10/2006 08:00");
		flight11.put("airline_code", "KL");
		flight11.put("airplane_type", "100");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ZRH");
		flight11.put("arrival", "AMS");
		flight11.put("departure_time", "15/10/2006 15:10");
		flight11.put("airline_code", "KL");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "AMS");
		flight11.put("arrival", "CPT");
		flight11.put("departure_time", "15/10/2006 21:35");
		flight11.put("airline_code", "KL");
		flight11.put("airplane_type", "744");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "CPT");
		flight11.put("arrival", "NBO");
		flight11.put("departure_time", "20/07/2008 07:40");
		flight11.put("airline_code", "KQ");
		flight11.put("airplane_type", "773");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "NBO");
		flight11.put("arrival", "ZNZ");
		flight11.put("departure_time", "20/07/2008 19:40");
		flight11.put("airline_code", "KQ");
		flight11.put("airplane_type", "737");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ZNZ");
		flight11.put("arrival", "ADD");
		flight11.put("departure_time", "30/07/2008 19:40");
		flight11.put("airline_code", "ET");
		flight11.put("airplane_type", "763");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ADD");
		flight11.put("arrival", "NBO");
		flight11.put("departure_time", "30/07/2008 23:15");
		flight11.put("airline_code", "ET");
		flight11.put("airplane_type", "752");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "NBO");
		flight11.put("arrival", "CPT");
		flight11.put("departure_time", "01/08/2008 10:10");
		flight11.put("airline_code", "KQ");
		flight11.put("airplane_type", "77W");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "JNB");
		flight11.put("arrival", "WDH");
		flight11.put("departure_time", "11/10/2010 06:40");
		flight11.put("airline_code", "SW");
		flight11.put("airplane_type", "333");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "WDH");
		flight11.put("arrival", "JNB");
		flight11.put("departure_time", "15/10/2010 20:40");
		flight11.put("airline_code", "SW");
		flight11.put("airplane_type", "332");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "CPT");
		flight11.put("arrival", "LAD");
		flight11.put("departure_time", "08/02/2012 16:10");
		flight11.put("airline_code", "DT");
		flight11.put("airplane_type", "737");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LAD");
		flight11.put("arrival", "GRU");
		flight11.put("departure_time", "08/02/2012 21:10");
		flight11.put("airline_code", "DT");
		flight11.put("airplane_type", "77W");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "GRU");
		flight11.put("arrival", "LAD");
		flight11.put("departure_time", "21/02/2012 17:15");
		flight11.put("airline_code", "DT");
		flight11.put("airplane_type", "77W");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LAD");
		flight11.put("arrival", "CPT");
		flight11.put("departure_time", "22/02/2012 03:45");
		flight11.put("airline_code", "DT");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "GRU");
		flight11.put("arrival", "GIG");
		flight11.put("departure_time", "09/02/2012 01:15");
		flight11.put("airline_code", "TM");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SDU");
		flight11.put("arrival", "CGH");
		flight11.put("departure_time", "20/02/2012 17:10");
		flight11.put("airline_code", "O6");
		flight11.put("airplane_type", "100");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "JNB");
		flight11.put("arrival", "ATL");
		flight11.put("departure_time", "20/06/2013 10:00");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "773");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ATL");
		flight11.put("arrival", "LAX");
		flight11.put("departure_time", "20/06/2013 20:15");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "752");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LAX");
		flight11.put("arrival", "HNL");
		flight11.put("departure_time", "21/06/2013 04:35");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "753");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HNL");
		flight11.put("arrival", "OGG");
		flight11.put("departure_time", "21/06/2013 12:00");
		flight11.put("airline_code", "HA");
		flight11.put("airplane_type", "717");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "OGG");
		flight11.put("arrival", "SFO");
		flight11.put("departure_time", "30/06/2013 17:00");
		flight11.put("airline_code", "HA");
		flight11.put("airplane_type", "332");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SFO");
		flight11.put("arrival", "ATL");
		flight11.put("departure_time", "30/06/2013 22:00");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "764");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ATL");
		flight11.put("arrival", "JNB");
		flight11.put("departure_time", "02/07/2013 10:20");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "77W");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "JNB");
		flight11.put("arrival", "MLE");
		flight11.put("departure_time", "02/08/2015 10:00");
		flight11.put("airline_code", "SA");
		flight11.put("airplane_type", "346");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "MLE");
		flight11.put("arrival", "JNB");
		flight11.put("departure_time", "12/08/2015 10:00");
		flight11.put("airline_code", "SA");
		flight11.put("airplane_type", "343");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "JNB");
		flight11.put("arrival", "CMB");
		flight11.put("departure_time", "05/10/2015 10:00");
		flight11.put("airline_code", "SA");
		flight11.put("airplane_type", "345");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "CMB");
		flight11.put("arrival", "DXB");
		flight11.put("departure_time", "10/10/2015 20:00");
		flight11.put("airline_code", "UL");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DXB");
		flight11.put("arrival", "CPT");
		flight11.put("departure_time", "11/10/2015 12:00");
		flight11.put("airline_code", "EK");
		flight11.put("airplane_type", "772");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SEZ");
		flight11.put("arrival", "PER");
		flight11.put("departure_time", "11/04/2016 12:00");
		flight11.put("airline_code", "HM");
		flight11.put("airplane_type", "333");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "CGK");
		flight11.put("arrival", "HLA");
		flight11.put("departure_time", "18/04/2016 12:00");
		flight11.put("airline_code", "GA");
		flight11.put("airplane_type", "773");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flightsUser1.put("flights", flightArray);
		flightList.add(flightsUser1);

		System.out.println(this.getClass().getSimpleName() + " | Added " + flightArray.size() + " flights to array for user <maria_cpt>");

		flightsUser1 = new JsonObject();
		flightsUser1.put("username", "green_madness");
		flightArray = new JsonArray();

		flight11 = new JsonObject();
		flight11.put("departure", "HNL");
		flight11.put("arrival", "LAX");
		flight11.put("departure_time", "17/04/1987 12:00");
		flight11.put("airline_code", "AA");
		flight11.put("airplane_type", "D10");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LAX");
		flight11.put("arrival", "HNL");
		flight11.put("departure_time", "27/04/1987 12:00");
		flight11.put("airline_code", "AA");
		flight11.put("airplane_type", "M11");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HNL");
		flight11.put("arrival", "ITO");
		flight11.put("departure_time", "20/05/1992 12:00");
		flight11.put("airline_code", "HA");
		flight11.put("airplane_type", "717");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ITO");
		flight11.put("arrival", "HNL");
		flight11.put("departure_time", "20/05/1992 12:00");
		flight11.put("airline_code", "HA");
		flight11.put("airplane_type", "AT7");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "HNL");
		flight11.put("arrival", "KOA");
		flight11.put("departure_time", "20/05/2002 12:00");
		flight11.put("airline_code", "HA");
		flight11.put("airplane_type", "CR7");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "KOA");
		flight11.put("arrival", "OGG");
		flight11.put("departure_time", "20/05/2002 17:00");
		flight11.put("airline_code", "HA");
		flight11.put("airplane_type", "CR2");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "OGG");
		flight11.put("arrival", "SEA");
		flight11.put("departure_time", "20/05/2002 20:00");
		flight11.put("airline_code", "UA");
		flight11.put("airplane_type", "D10");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SEA");
		flight11.put("arrival", "DFW");
		flight11.put("departure_time", "25/05/2002 20:00");
		flight11.put("airline_code", "UA");
		flight11.put("airplane_type", "764");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DFW");
		flight11.put("arrival", "HNL");
		flight11.put("departure_time", "25/05/2002 20:00");
		flight11.put("airline_code", "UA");
		flight11.put("airplane_type", "752");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HNL");
		flight11.put("arrival", "NAN");
		flight11.put("departure_time", "25/05/2012 20:00");
		flight11.put("airline_code", "FJ");
		flight11.put("airplane_type", "332");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HNL");
		flight11.put("arrival", "OGG");
		flight11.put("departure_time", "25/05/2012 20:00");
		flight11.put("airline_code", "FJ");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HNL");
		flight11.put("arrival", "YVR");
		flight11.put("departure_time", "10/07/2015 20:00");
		flight11.put("airline_code", "AC");
		flight11.put("airplane_type", "763");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "PDX");
		flight11.put("arrival", "HNL");
		flight11.put("departure_time", "14/07/2015 20:00");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "753");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HNL");
		flight11.put("arrival", "BNE");
		flight11.put("departure_time", "28/03/2016 20:00");
		flight11.put("airline_code", "HA");
		flight11.put("airplane_type", "332");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SYD");
		flight11.put("arrival", "HNL");
		flight11.put("departure_time", "12/04/2016 08:25");
		flight11.put("airline_code", "JQ");
		flight11.put("airplane_type", "788");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flightsUser1.put("flights", flightArray);
		flightList.add(flightsUser1);

		System.out.println(this.getClass().getSimpleName() + " | Added " + flightArray.size() + " flights to array for user <green_madness>");

		flightsUser1 = new JsonObject();
		flightsUser1.put("username", "johnny_acid");
		flightArray = new JsonArray();

		flight11 = new JsonObject();
		flight11.put("departure", "LGA");
		flight11.put("arrival", "YUL");
		flight11.put("departure_time", "10/01/2016 07:15");
		flight11.put("airline_code", "VA");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "YEG");
		flight11.put("arrival", "EWR");
		flight11.put("departure_time", "17/01/2016 10:15");
		flight11.put("airline_code", "UA");
		flight11.put("airplane_type", "752");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "JFK");
		flight11.put("arrival", "FLL");
		flight11.put("departure_time", "23/01/2016 10:15");
		flight11.put("airline_code", "B6");
		flight11.put("airplane_type", "E90");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "FLL");
		flight11.put("arrival", "DTW");
		flight11.put("departure_time", "26/01/2016 10:15");
		flight11.put("airline_code", "B6");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "JFK");
		flight11.put("arrival", "SJC");
		flight11.put("departure_time", "11/02/2016 10:15");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "739");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SJC");
		flight11.put("arrival", "LAS");
		flight11.put("departure_time", "14/02/2016 06:20");
		flight11.put("airline_code", "AA");
		flight11.put("airplane_type", "E70");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LAS");
		flight11.put("arrival", "LGA");
		flight11.put("departure_time", "18/02/2016 21:55");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "753");
		flight11.put("rating", 4);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "JFK");
		flight11.put("arrival", "TPE");
		flight11.put("departure_time", "10/03/2016 07:00");
		flight11.put("airline_code", "BR");
		flight11.put("airplane_type", "77W");
		flight11.put("rating", 5);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "TPE");
		flight11.put("arrival", "MFM");
		flight11.put("departure_time", "12/03/2016 11:00");
		flight11.put("airline_code", "CA");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "MFM");
		flight11.put("arrival", "PEK");
		flight11.put("departure_time", "14/03/2016 13:00");
		flight11.put("airline_code", "CA");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 2);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "PEK");
		flight11.put("arrival", "JFK");
		flight11.put("departure_time", "20/03/2016 13:00");
		flight11.put("airline_code", "CZ");
		flight11.put("airplane_type", "388");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "LAX");
		flight11.put("arrival", "SZX");
		flight11.put("departure_time", "11/04/2016 14:45");
		flight11.put("airline_code", "ME");
		flight11.put("airplane_type", "332");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SZX");
		flight11.put("arrival", "DPS");
		flight11.put("departure_time", "14/04/2016 14:45");
		flight11.put("airline_code", "AK");
		flight11.put("airplane_type", "320");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "DPS");
		flight11.put("arrival", "MNL");
		flight11.put("departure_time", "16/04/2016 22:00");
		flight11.put("airline_code", "PR");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "airline");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "MNL");
		flight11.put("arrival", "BOS");
		flight11.put("departure_time", "16/04/2016 22:00");
		flight11.put("airline_code", "PR");
		flight11.put("airplane_type", "343");
		flight11.put("rating", 5);
		flight11.put("flight_class", 2);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "OAK");
		flight11.put("arrival", "ADL");
		flight11.put("departure_time", "16/04/2015 22:00");
		flight11.put("airline_code", "QA");
		flight11.put("airplane_type", "388");
		flight11.put("rating", 5);
		flight11.put("flight_class", 2);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ADL");
		flight11.put("arrival", "HRB");
		flight11.put("departure_time", "18/04/2015 18:00");
		flight11.put("airline_code", "AK");
		flight11.put("airplane_type", "333");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "HRB");
		flight11.put("arrival", "ULN");
		flight11.put("departure_time", "18/04/2015 18:00");
		flight11.put("airline_code", "OM");
		flight11.put("airplane_type", "763");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ULN");
		flight11.put("arrival", "ALA");
		flight11.put("departure_time", "25/04/2015 18:00");
		flight11.put("airline_code", "KC");
		flight11.put("airplane_type", "321");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ALA");
		flight11.put("arrival", "CTU");
		flight11.put("departure_time", "25/04/2015 18:00");
		flight11.put("airline_code", "CA");
		flight11.put("airplane_type", "738");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "ALA");
		flight11.put("arrival", "SAN");
		flight11.put("departure_time", "30/04/2015 18:00");
		flight11.put("airline_code", "UA");
		flight11.put("airplane_type", "788");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "SAN");
		flight11.put("arrival", "IAD");
		flight11.put("departure_time", "30/10/2015 11:00");
		flight11.put("airline_code", "AA");
		flight11.put("airplane_type", "763");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "IAD");
		flight11.put("arrival", "PHL");
		flight11.put("departure_time", "03/11/2015 12:00");
		flight11.put("airline_code", "AA");
		flight11.put("airplane_type", "DH4");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "IAD");
		flight11.put("arrival", "JFK");
		flight11.put("departure_time", "03/11/2015 16:00");
		flight11.put("airline_code", "AA");
		flight11.put("airplane_type", "E90");
		flight11.put("rating", 4);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "JFK");
		flight11.put("arrival", "KEF");
		flight11.put("departure_time", "05/11/2015 03:00");
		flight11.put("airline_code", "FI");
		flight11.put("airplane_type", "752");
		flight11.put("rating", 5);
		flight11.put("flight_class", 1);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);


		flight11 = new JsonObject();
		flight11.put("departure", "KEF");
		flight11.put("arrival", "AEY");
		flight11.put("departure_time", "06/11/2015 19:00");
		flight11.put("airline_code", "FI");
		flight11.put("airplane_type", "AT7");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "AEY");
		flight11.put("arrival", "RKV");
		flight11.put("departure_time", "09/11/2015 14:00");
		flight11.put("airline_code", "FI");
		flight11.put("airplane_type", "AT7");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "KEF");
		flight11.put("arrival", "YYZ");
		flight11.put("departure_time", "12/11/2015 23:00");
		flight11.put("airline_code", "FI");
		flight11.put("airplane_type", "752");
		flight11.put("rating", 5);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "YYZ");
		flight11.put("arrival", "YYC");
		flight11.put("departure_time", "18/11/2015 07:00");
		flight11.put("airline_code", "CA");
		flight11.put("airplane_type", "E70");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 1);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flight11 = new JsonObject();
		flight11.put("departure", "YYC");
		flight11.put("arrival", "LGA");
		flight11.put("departure_time", "25/12/2015 08:00");
		flight11.put("airline_code", "DL");
		flight11.put("airplane_type", "E70");
		flight11.put("rating", 3);
		flight11.put("flight_class", 0);
		flight11.put("purpose", 0);
		flight11.put("ticket_source", "agent");
		flightArray.add(flight11);

		flightsUser1.put("flights", flightArray);
		flightList.add(flightsUser1);

		System.out.println(this.getClass().getSimpleName() + " | Added " + flightArray.size() + " flights to array for user <johnny_acid>");


		handler.handle(null);
	}
}
