package frequentFlyer.flight_diary.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * Flight Date Formatter
 * 
 * This module keeps all flight dates in database in the same format
 * 
 * @author Sasa Radovanovic
 *
 */
public class FlightDateFormatter {

	private static DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	public static final String formatDate (Date date) {
		return df.format(date);
	}
	
}
