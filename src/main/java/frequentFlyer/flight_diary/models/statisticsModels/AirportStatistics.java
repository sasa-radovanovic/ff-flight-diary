package frequentFlyer.flight_diary.models.statisticsModels;

import java.util.HashMap;

/**
 * @author Sasa Radovanovic
 *
 */
public class AirportStatistics {
	
	private int totalUsedAirports;
	private int northernAirports;
	private int southernAirports;
	
	private HashMap<String, Integer> byRegionMap;
	private HashMap<String, Integer> mostUsedDepartures;
	private HashMap<String, Integer> mostUsedArrival;
	
	public AirportStatistics() {
		super();
		this.totalUsedAirports = 0;
		this.northernAirports = 0;
		this.southernAirports = 0;
		this.byRegionMap = new HashMap<>();
		this.mostUsedDepartures = new HashMap<>();
		this.mostUsedArrival = new HashMap<>();
	}

	public int getTotalUsedAirports() {
		return totalUsedAirports;
	}

	public void setTotalUsedAirports(int totalUsedAirports) {
		this.totalUsedAirports = totalUsedAirports;
	}

	public int getNorthernAirports() {
		return northernAirports;
	}

	public void setNorthernAirports(int northernAirports) {
		this.northernAirports = northernAirports;
	}

	public int getSouthernAirports() {
		return southernAirports;
	}

	public void setSouthernAirports(int southernAirports) {
		this.southernAirports = southernAirports;
	}
	
	public void increaseRegion (String region) {
		if (this.byRegionMap.containsKey(region)) {
			int val = this.byRegionMap.get(region) + 1;
			this.byRegionMap.replace(region, val);
		} else {
			this.byRegionMap.put(region, 1);
		}
	}
	

	public HashMap<String, Integer> getByRegionMap() {
		return byRegionMap;
	}

	public void setByRegionMap(HashMap<String, Integer> byRegionMap) {
		this.byRegionMap = byRegionMap;
	}

	public HashMap<String, Integer> getMostUsedDepartures() {
		return mostUsedDepartures;
	}

	public void setMostUsedDepartures(HashMap<String, Integer> mostUsedDepartures) {
		this.mostUsedDepartures = mostUsedDepartures;
	}

	public HashMap<String, Integer> getMostUsedArrival() {
		return mostUsedArrival;
	}

	public void setMostUsedArrival(HashMap<String, Integer> mostUsedArrival) {
		this.mostUsedArrival = mostUsedArrival;
	}
	
}
