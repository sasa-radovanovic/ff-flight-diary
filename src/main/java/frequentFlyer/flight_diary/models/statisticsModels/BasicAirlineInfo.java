package frequentFlyer.flight_diary.models.statisticsModels;

/**
 * @author Sasa Radovanovic
 *
 */
public class BasicAirlineInfo {

	private String airline_name;
	private String airline_code;
	private int count;
	
	public BasicAirlineInfo() {
		super();
	}

	public BasicAirlineInfo(String airline_name, String airline_code, int count) {
		super();
		this.airline_name = airline_name;
		this.airline_code = airline_code;
		this.count = count;
	}

	public String getAirline_name() {
		return airline_name;
	}

	public void setAirline_name(String airline_name) {
		this.airline_name = airline_name;
	}

	public String getAirline_code() {
		return airline_code;
	}

	public void setAirline_code(String airline_code) {
		this.airline_code = airline_code;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
