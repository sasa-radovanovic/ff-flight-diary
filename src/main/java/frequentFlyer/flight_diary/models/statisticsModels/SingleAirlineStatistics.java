package frequentFlyer.flight_diary.models.statisticsModels;

import java.util.HashMap;

/**
 * @author Sasa Radovanovic
 *
 */
public class SingleAirlineStatistics {
	
	private HashMap<String, Integer> frequentUsers;
	private int totalFlights;
	private double averageRating;
	private HashMap<String, BasicFlightInfo> flightsList;
	
	public SingleAirlineStatistics() {
		super();
		this.totalFlights = 0;
		this.averageRating = -1;
		this.frequentUsers = new HashMap<>();
		this.flightsList = new HashMap<>();
	}

	public SingleAirlineStatistics(HashMap<String, Integer> frequentUsers,
			int totalFlights, double averageRating,
			HashMap<String, BasicFlightInfo> flightsList) {
		super();
		this.frequentUsers = frequentUsers;
		this.totalFlights = totalFlights;
		this.averageRating = averageRating;
		this.flightsList = flightsList;
	}

	public HashMap<String, Integer> getFrequentUsers() {
		return frequentUsers;
	}

	public void setFrequentUsers(HashMap<String, Integer> frequentUsers) {
		this.frequentUsers = frequentUsers;
	}

	public int getTotalFlights() {
		return totalFlights;
	}

	public void setTotalFlights(int totalFlights) {
		this.totalFlights = totalFlights;
	}

	public double getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(double averageRating) {
		this.averageRating = averageRating;
	}

	public HashMap<String, BasicFlightInfo> getFlightsList() {
		return flightsList;
	}

	public void setFlightsList(HashMap<String, BasicFlightInfo> flightsList) {
		this.flightsList = flightsList;
	}
	
	public void increaseUser (String username) {
		if (this.frequentUsers.containsKey(username)) {
			int val = (this.frequentUsers.get(username)) + 1;
			this.frequentUsers.replace(username, val);
		} else {
			this.frequentUsers.put(username, 1);
		}
	}

}
