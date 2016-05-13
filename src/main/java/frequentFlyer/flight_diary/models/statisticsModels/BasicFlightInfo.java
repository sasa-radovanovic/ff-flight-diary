package frequentFlyer.flight_diary.models.statisticsModels;

/**
 * @author Sasa Radovanovic
 *
 */
public class BasicFlightInfo {

	private double longitude_one;
	private double latitude_one;
	private double longitude_two;
	private double latitude_two;

	public BasicFlightInfo() {
		super();
		this.longitude_two = -1;
		this.longitude_one = -1;
		this.latitude_one = -1;
		this.latitude_two = -1;
	}

	public BasicFlightInfo(double longitude_one, double latitude_one,
			double longitude_two, double latitude_two) {
		super();
		this.longitude_one = longitude_one;
		this.latitude_one = latitude_one;
		this.longitude_two = longitude_two;
		this.latitude_two = latitude_two;
	}

	public double getLongitude_one() {
		return longitude_one;
	}

	public void setLongitude_one(double longitude_one) {
		this.longitude_one = longitude_one;
	}

	public double getLatitude_one() {
		return latitude_one;
	}

	public void setLatitude_one(double latitude_one) {
		this.latitude_one = latitude_one;
	}

	public double getLongitude_two() {
		return longitude_two;
	}

	public void setLongitude_two(double longitude_two) {
		this.longitude_two = longitude_two;
	}

	public double getLatitude_two() {
		return latitude_two;
	}

	public void setLatitude_two(double latitude_two) {
		this.latitude_two = latitude_two;
	}

	
}
