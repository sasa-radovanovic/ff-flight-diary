package frequentFlyer.flight_diary.utils;

import frequentFlyer.flight_diary.models.Airline;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 * Airline parser
 * 
 * Module which parses CSV file containing airline data and stores in to Database
 * 
 * @author Sasa Radovanovic
 *
 */
public class AirlinesParser {

	private static int inserted = 0;

	/**
	 * @param location - Location of CSV on the file system
	 * @param jdbc - JDBC connector to use when creating SQL connection
	 * @param handler - Callback on job done
	 */
	public static void parseCSV(String location, JDBCClient jdbc, Handler<Boolean> handler) {
		jdbc.getConnection(res -> {

			if (res.failed()) {
				throw new RuntimeException(res.cause());
			}
			final SQLConnection conn = res.result();
			if (conn == null) {
				System.err.println("Connection is null!");
				throw new RuntimeException(res.cause());
			}


			BufferedReader br = null;
			String line = "";
			String cvsSplitBy = ",";
			int skippedAirportsEmptyIATA = 0;
			int skippedAirportsFormatIATA = 0;

			try {

				br = new BufferedReader(new FileReader(location));
				while ((line = br.readLine()) != null) {

					// use comma as separator
					String[] airline = line.split(cvsSplitBy);

					String iata_code = skipSymbols(airline[3]);

					if (!iata_code.equalsIgnoreCase("") && iata_code.length() == 2 && skipSymbols(airline[7]).equalsIgnoreCase("Y")) {
						
						Airline airlineToInsert = new Airline(skipSymbols(airline[1]), iata_code, 
								skipSymbols(airline[4]), skipSymbols(airline[6]), skipSymbols(airline[5]));

						insertAirline (conn, airlineToInsert);
					}

				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			System.out.println("Done Skipped " + skippedAirportsEmptyIATA + "/" + skippedAirportsFormatIATA);

			handler.handle(true);
		});
	}

	/**
	 * Helper method
	 */
	private static String skipSymbols (String input) {
		return input.substring(1, input.length() - 1);
	}


	/**
	 * @param conn - SQL connection to use when inserting new airline
	 * @param airlineToInsert - Airline object to insert to DB
	 */
	private static void insertAirline (SQLConnection conn, Airline airlineToInsert) {
		
			conn.updateWithParams("INSERT INTO flight_diary_airlines (airline_name, airline_iata_code, airline_icao_code, airline_country, airline_callsign) "
					+ "VALUES (?, ?, ?, ?, ?)", 
					new JsonArray().add(airlineToInsert.getAirline_name()).add(airlineToInsert.getAirline_iata_code())
						.add(airlineToInsert.getAirline_icao_code())
						.add(airlineToInsert.getAirline_country())
						.add(airlineToInsert.getAirline_callsign()), airlineInsertQuery -> {
						if (airlineInsertQuery.failed()) {
							System.err.println("insertAirline | FAILED " + airlineToInsert.getAirline_name());
						} else if (airlineInsertQuery.succeeded()) {
							System.out.println("insertAirline | Imported airline " + ++inserted);
						}
					});
	}

}
