package frequentFlyer.flight_diary.models.statisticsModels;

import java.util.HashMap;

/**
 * @author Sasa Radovanovic
 *
 */
public class UserStatisics {
	
	private HashMap<String, Integer> yearDistributionMap;
	private HashMap<String, Integer> monthDistributionMap;
	private HashMap<String, Integer> timeDistributionMap;
	private HashMap<String, Integer> regionDistributionMap;
	
	
	
	public UserStatisics() {
		super();
		yearDistributionMap = new HashMap<>();
		monthDistributionMap = new HashMap<>();
		timeDistributionMap = new HashMap<>();
		regionDistributionMap = new HashMap<>();
	}



	public UserStatisics(HashMap<String, Integer> yearDistributionMap,
			HashMap<String, Integer> monthDistributionMap,
			HashMap<String, Integer> timeDistributionMap,
			HashMap<String, Integer> regionDistributionMap) {
		super();
		this.yearDistributionMap = yearDistributionMap;
		this.monthDistributionMap = monthDistributionMap;
		this.timeDistributionMap = timeDistributionMap;
		this.regionDistributionMap = regionDistributionMap;
	}



	public HashMap<String, Integer> getYearDistributionMap() {
		return yearDistributionMap;
	}



	public void setYearDistributionMap(HashMap<String, Integer> yearDistributionMap) {
		this.yearDistributionMap = yearDistributionMap;
	}



	public HashMap<String, Integer> getMonthDistributionMap() {
		return monthDistributionMap;
	}



	public void setMonthDistributionMap(
			HashMap<String, Integer> monthDistributionMap) {
		this.monthDistributionMap = monthDistributionMap;
	}



	public HashMap<String, Integer> getTimeDistributionMap() {
		return timeDistributionMap;
	}



	public void setTimeDistributionMap(HashMap<String, Integer> timeDistributionMap) {
		this.timeDistributionMap = timeDistributionMap;
	}



	public HashMap<String, Integer> getRegionDistributionMap() {
		return regionDistributionMap;
	}



	public void setRegionDistributionMap(
			HashMap<String, Integer> continentDistributionMap) {
		this.regionDistributionMap = continentDistributionMap;
	}
	
	

	public void increaseYear (String year) {
		if (this.yearDistributionMap.containsKey(year)) {
			int val = this.yearDistributionMap.get(year) + 1;
			this.yearDistributionMap.replace(year, val);
		} else {
			this.yearDistributionMap.put(year, 1);
		}
	}
	
	public void increaseRegion  (String region) {
		if (this.regionDistributionMap.containsKey(region)) {
			int val = this.regionDistributionMap.get(region) + 1;
			this.regionDistributionMap.replace(region, val);
		} else {
			this.regionDistributionMap.put(region, 1);
		}
	}
	
	public void increaseMonth (String month) {
		if (this.monthDistributionMap.containsKey(month)) {
			int val = this.monthDistributionMap.get(month) + 1;
			this.monthDistributionMap.replace(month, val);
		} else {
			this.monthDistributionMap.put(month, 1);
		}
	}
	
	public void increaseTime (String time) {
		if (this.timeDistributionMap.containsKey(time)) {
			int val = this.timeDistributionMap.get(time) + 1;
			this.timeDistributionMap.replace(time, val);
		} else {
			this.timeDistributionMap.put(time, 1);
		}
	}
	
}
