package frequentFlyer.flight_diary.models.statisticsModels;

/**
 * @author Sasa Radovanovic
 *
 */
public class BasicAirportInfo {

	private String iata_code;
	
	private double longitude;
	
	private double latitude;
	
	public BasicAirportInfo(String iata_code,
			double longitude, double latitude) {
		super();
		this.iata_code = iata_code;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public BasicAirportInfo() {
		super();
	}

	public String getIata_code() {
		return iata_code;
	}

	public void setIata_code(String iata_code) {
		this.iata_code = iata_code;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	
}
