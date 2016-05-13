package frequentFlyer.flight_diary.models.statisticsModels;

/**
 * @author Sasa Radovanovic
 *
 */
public class BasicAirplaneTypeInfo {

	private String airplane_name;
	private String airplane_code;
	private int count;
	
	public BasicAirplaneTypeInfo(String airplane_name, String airplane_code, int count) {
		super();
		this.airplane_name = airplane_name;
		this.airplane_code = airplane_code;
		this.count = count;
	}

	public BasicAirplaneTypeInfo() {
		super();
	}

	public String getAirplane_name() {
		return airplane_name;
	}

	public void setAirplane_name(String airplane_name) {
		this.airplane_name = airplane_name;
	}

	public String getAirplane_code() {
		return airplane_code;
	}

	public void setAirplane_code(String airplane_code) {
		this.airplane_code = airplane_code;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
}
