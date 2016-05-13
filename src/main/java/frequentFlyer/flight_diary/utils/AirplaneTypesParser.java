package frequentFlyer.flight_diary.utils;

import frequentFlyer.flight_diary.models.AirplaneType;
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
 * Airplane Type parser
 * 
 * Module which parses CSV file containing types of airplanes and stores in to Database
 * 
 * @author Sasa Radovanovic
 *
 */
public class AirplaneTypesParser {
	
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
			int skippedTypes = 0;

			try {

				br = new BufferedReader(new FileReader(location));
				while ((line = br.readLine()) != null) {

					// use comma as separator
					String[] airplaneType = line.split(cvsSplitBy);

					String type_code = airplaneType[0];

					if (!type_code.equalsIgnoreCase("") && type_code.length() == 3) {
						insertType(conn, new AirplaneType(type_code, airplaneType[1]));
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



			System.out.println("Done Skipped " + skippedTypes);

			handler.handle(true);
		});
	}


	/**
	 * @param conn - SQL connection to use when inserting new airline
	 * @param airplaneType - AirplaneType object to insert to DB
	 */
	private static void insertType (SQLConnection conn, AirplaneType airplaneType) {
		conn.updateWithParams("INSERT INTO flight_diary_airplane_types (type_code, type_name) "
				+ "VALUES (?, ?)", 
				new JsonArray().add(airplaneType.getType_code()).add(airplaneType.getType_name()), airplaneTypeInserted -> {
					if (airplaneTypeInserted.failed()) {
						System.err.println("insertType | FAILED " + airplaneType.getType_code());
					} else if (airplaneTypeInserted.succeeded()) {
						System.out.println("insertAirport | Imported airport " + ++inserted);
					}
				});
	}


}
