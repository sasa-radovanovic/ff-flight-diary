package frequentFlyer.flight_diary.utils;

/**
 * 
 * Calculator of distance between two geospatial points
 * 
 * @author Sasa Radovanovic
 *
 */
public class FlightDataCalculator {
	
	/**
	 * @param lat1 - Latitude 
	 * @param lon1 - Longitude 
	 * @param lat2 - Latitude
	 * @param lon2 - Longitude
	 * @return - Double value representing distance in km between two geo-spatial points
	 */
	public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344;
		return (dist);
	}

	
	/**
	 * @param deg - This function converts decimal degrees to radians
	 * @return
	 */
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/**
	 * @param rad - This function converts radians to decimal degrees	
	 * @return
	 */
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}
}
