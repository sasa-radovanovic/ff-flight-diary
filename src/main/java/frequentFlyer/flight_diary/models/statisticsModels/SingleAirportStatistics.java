package frequentFlyer.flight_diary.models.statisticsModels;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Sasa Radovanovic
 *
 */
public class SingleAirportStatistics {
	
	private int visited;
	
	private double averageRating;
	
	private ArrayList<BasicAirportInfo> connectionAirports;
	
	private HashMap<String, Integer> usersOnAirport;

	public SingleAirportStatistics() {
		super();
		this.visited = 0;
		this.averageRating = -1;
		this.connectionAirports = null;
		this.usersOnAirport = new HashMap<>();
	}


	public SingleAirportStatistics(int visited, double averageRating,
			ArrayList<BasicAirportInfo> connectionAirports,
			HashMap<String, Integer> usersOnAirport) {
		super();
		this.visited = visited;
		this.averageRating = averageRating;
		this.connectionAirports = connectionAirports;
		this.usersOnAirport = usersOnAirport;
	}


	public int getVisited() {
		return visited;
	}


	public void setVisited(int visited) {
		this.visited = visited;
	}


	public double getAverageRating() {
		return averageRating;
	}


	public void setAverageRating(double averageRating) {
		this.averageRating = averageRating;
	}


	public ArrayList<BasicAirportInfo> getConnectionAirports() {
		return connectionAirports;
	}


	public void setConnectionAirports(ArrayList<BasicAirportInfo> connectionAirports) {
		this.connectionAirports = connectionAirports;
	}


	public HashMap<String, Integer> getUsersOnAirport() {
		return usersOnAirport;
	}


	public void setUsersOnAirport(HashMap<String, Integer> usersOnAirport) {
		this.usersOnAirport = usersOnAirport;
	}
	
	public void increaseUsersActivityOnAirport (String username) {
		if (this.usersOnAirport.containsKey(username)) {
			int timesUserOnAirport = this.usersOnAirport.get(username) + 1;
			this.usersOnAirport.replace(username, timesUserOnAirport);
		} else {
			this.usersOnAirport.put(username, 1);
		}
	}
	
}
