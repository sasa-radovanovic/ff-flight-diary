package frequentFlyer.flight_diary.models.cacheModels;

import java.util.ArrayList;

/**
 * 
 * Model for cache
 * 
 * @author Sasa Radovanovic
 *
 */
public class FlightData {

	private double distance;
	
	private double departure_longitude;
	
	private double departure_latitude;
	
	private double arrival_longitude;
	
	private double arrival_latitude;
	
	private ArrayList<FlightUser> usersRecentlyAdded;
	

	public FlightData() {
		super();
	}

	public FlightData(double distance, ArrayList<FlightUser> usersRecentlyAdded) {
		super();
		this.distance = distance;
		this.usersRecentlyAdded = usersRecentlyAdded;
	}
	
	public FlightData(double distance, ArrayList<FlightUser> usersRecentlyAdded, double departureLong, double departureLat, double arrivalLong, double arrivalLat) {
		super();
		this.distance = distance;
		this.usersRecentlyAdded = usersRecentlyAdded;
		this.departure_longitude = departureLong;
		this.departure_latitude = departureLat;
		this.arrival_longitude = arrivalLong;
		this.arrival_latitude = arrivalLat;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public ArrayList<FlightUser> getUsersRecentlyAdded() {
		return usersRecentlyAdded;
	}

	public void setUsersRecentlyAdded(ArrayList<FlightUser> usersRecentlyAdded) {
		this.usersRecentlyAdded = usersRecentlyAdded;
	}
	
	public double getDeparture_longitude() {
		return departure_longitude;
	}

	public void setDeparture_longitude(double departure_longitude) {
		this.departure_longitude = departure_longitude;
	}

	public double getDeparture_latitude() {
		return departure_latitude;
	}

	public void setDeparture_latitude(double departure_latitude) {
		this.departure_latitude = departure_latitude;
	}

	public double getArrival_longitude() {
		return arrival_longitude;
	}

	public void setArrival_longitude(double arrival_longitude) {
		this.arrival_longitude = arrival_longitude;
	}

	public double getArrival_latitude() {
		return arrival_latitude;
	}

	public void setArrival_latitude(double arrival_latitude) {
		this.arrival_latitude = arrival_latitude;
	}

	@Override
	public String toString() {
		String userData = "";
		for (int i=0; i<usersRecentlyAdded.size(); i++) {
			FlightUser flightUser = usersRecentlyAdded.get(i);
			userData += userData + " [" + flightUser.getUsername() + " <> " + flightUser.getTimeAdded() + "] ";
		}
		System.out.println("FlightData [distance=" + distance + ", usersRecentlyAdded="
				+ userData + "]");
		return "FlightData [distance=" + distance + ", usersRecentlyAdded="
				+ userData + "]";
	}
	
	
	
}
