package frequentFlyer.flight_diary.models.statisticsModels;

import java.util.ArrayList;

/**
 * @author Sasa Radovanovic
 *
 */
public class GeneralStatistics {
	
	ArrayList<BasicAirlineInfo> airlines;
	ArrayList<BasicAirplaneTypeInfo> airplaneTypes;
	
	
	
	public GeneralStatistics() {
		super();
		this.airlines = new ArrayList<>();
		this.airplaneTypes = new ArrayList<>();
	}

	public GeneralStatistics(ArrayList<BasicAirlineInfo> airlines,
			ArrayList<BasicAirplaneTypeInfo> airplaneTypes) {
		super();
		this.airlines = airlines;
		this.airplaneTypes = airplaneTypes;
	}

	public ArrayList<BasicAirlineInfo> getAirlines() {
		return airlines;
	}

	public void setAirlines(ArrayList<BasicAirlineInfo> airlines) {
		this.airlines = airlines;
	}

	public ArrayList<BasicAirplaneTypeInfo> getAirplaneTypes() {
		return airplaneTypes;
	}

	public void setAirplaneTypes(ArrayList<BasicAirplaneTypeInfo> airplaneTypes) {
		this.airplaneTypes = airplaneTypes;
	}

}
