package frequentFlyer.flight_diary.utils;

import frequentFlyer.flight_diary.models.Airport;
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
 * Airport parser
 * 
 * Module which parses CSV file containing airports and stores in to Database
 * 
 * @author Sasa Radovanovic
 *
 */
public class AirportsParser {

	private static int inserted = 0;

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
				while ((line = br.readLine()) != null && !line.equalsIgnoreCase("")) {

					// use comma as separator
					String[] airport = line.split(cvsSplitBy);

					String iata_code = skipSymbols(airport[4]);
					
					if (iata_code.equalsIgnoreCase("")) {
						skippedAirportsEmptyIATA ++;
					} else if (iata_code.length() > 0 && iata_code.length() != 3) {
						skippedAirportsFormatIATA ++;
					} else {


						Airport airportToInsert = new Airport(skipSymbols(airport[1]), skipSymbols(airport[2]), 
								skipSymbols(airport[3]), skipSymbols(airport[4]), skipSymbols(airport[5]), 
								Double.parseDouble(airport[6]), Double.parseDouble(airport[7]), Double.parseDouble(airport[8]), Double.parseDouble(airport[9]), skipSymbols(airport[11]));


						insertAirport (conn, airportToInsert, handle -> {
							if (airportToInsert.getIata_code().equalsIgnoreCase("OLT")) {
								handler.handle(true);
							}
						});
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
						conn.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println("Done Skipped " + skippedAirportsEmptyIATA + "/" + skippedAirportsFormatIATA);
			handler.handle(true);
		});
	}

	private static String skipSymbols (String input) {
		return input.substring(1, input.length() - 1);
	}


	/**
	 * @param conn - SQL connection to use when inserting new airline
	 * @param airportToInsert - Airport object to insert to DB
	 */
	private static void insertAirport (SQLConnection conn, Airport airportToInsert, Handler<Void> handle) {
		
			conn.updateWithParams("INSERT INTO flight_diary_airports (airport_name, airport_location, iata_code, icao_code, longitude, latitude, altitude, utc_offset, area_timezone) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", 
					new JsonArray().add(airportToInsert.getAirport_name()).add(airportToInsert.getAirport_location())
					.add(airportToInsert.getIata_code()).add(airportToInsert.getIcao_code()).add(airportToInsert.getLongitude())
					.add(airportToInsert.getLatitude()).add(airportToInsert.getAltitude()).add(airportToInsert.getUtc_offset())
					.add(airportToInsert.getArea_timezone()), airportInsertQuery -> {
						if (airportInsertQuery.failed()) {
							System.err.println("insertAirport | FAILED " + airportToInsert.getAirport_name() + " [" + airportToInsert.getIata_code() + "]");
							handle.handle(null);
						} else if (airportInsertQuery.succeeded()) {
							System.out.println("insertAirport | Imported airport " + airportToInsert.getIata_code() + " " + ++inserted);
							handle.handle(null);
						}
					});
	}

}
