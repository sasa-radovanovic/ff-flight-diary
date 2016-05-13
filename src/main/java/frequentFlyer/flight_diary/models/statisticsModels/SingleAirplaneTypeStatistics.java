package frequentFlyer.flight_diary.models.statisticsModels;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Sasa Radovanovic
 *
 */
public class SingleAirplaneTypeStatistics {
	
	private int totalFlights;
	private ArrayList<String> usersFlown;
	private double averageRating;
	private HashMap<String, Integer> airports;
	
	public SingleAirplaneTypeStatistics() {
		super();
		this.totalFlights = 0;
		this.averageRating = -1;
		this.airports = new HashMap<>();
		this.usersFlown = new ArrayList<>();
	}

	public SingleAirplaneTypeStatistics(int totalFlights,
			ArrayList<String> usersFlown, double averageRating,
			HashMap<String, Integer> airports) {
		super();
		this.totalFlights = totalFlights;
		this.usersFlown = usersFlown;
		this.averageRating = averageRating;
		this.airports = airports;
	}

	public int getTotalFlights() {
		return totalFlights;
	}

	public void setTotalFlights(int totalFlights) {
		this.totalFlights = totalFlights;
	}

	public ArrayList<String> getUsersFlown() {
		return usersFlown;
	}

	public void setUsersFlown(ArrayList<String> usersFlown) {
		this.usersFlown = usersFlown;
	}

	public double getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(double averageRating) {
		this.averageRating = averageRating;
	}

	public HashMap<String, Integer> getAirports() {
		return airports;
	}

	public void setAirports(HashMap<String, Integer> airports) {
		this.airports = airports;
	}
	
	public void increaseAirport (String airport_code) {
		if (this.airports.containsKey(airport_code)) {
			int val = this.airports.get(airport_code) + 1;
			this.airports.replace(airport_code, val);
		} else {
			this.airports.put(airport_code, 1);
		}
	}
	
}
