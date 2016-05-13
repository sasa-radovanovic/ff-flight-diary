package frequentFlyer.flight_diary.models;

import io.vertx.core.json.JsonObject;

/**
 * @author Sasa Radovanovic
 *
 */
public class Airport {
	
	String airport_name;
	
	String airport_location;
	
	String country;
	
	String iata_code;
	
	String icao_code;
	
	Double longitude;
	
	Double latitude;
	
	Double altitude;
	
	Double utc_offset;
	
	String area_timezone;
	
	public Airport() {
		super();
	}
	
	public Airport(JsonObject jsonObject) {
		if (jsonObject.containsKey("airport_name")) {
			this.airport_name = jsonObject.getString("airport_name");
		}
		if (jsonObject.containsKey("airport_location")) {
			this.airport_location = jsonObject.getString("airport_location");
		}
		if (jsonObject.containsKey("country")) {
			this.country = jsonObject.getString("country");
		}
		if (jsonObject.containsKey("iata_code")) {
			this.iata_code = jsonObject.getString("iata_code");
		}
		if (jsonObject.containsKey("icao_code")) {
			this.icao_code = jsonObject.getString("icao_code");
		}
		if (jsonObject.containsKey("longitude")) {
			this.longitude = jsonObject.getDouble("longitude");
		}
		if (jsonObject.containsKey("latitude")) {
			this.latitude = jsonObject.getDouble("latitude");
		}
		if (jsonObject.containsKey("altitude")) {
			this.altitude = jsonObject.getDouble("altitude");
		}
		if (jsonObject.containsKey("utc_offset")) {
			this.utc_offset = jsonObject.getDouble("utc_offset");
		}
		if (jsonObject.containsKey("area_timezone")) {
			this.area_timezone = jsonObject.getString("area_timezone");
		}
	}

	public Airport(String airportName, String airportLocation, String country,
			String iata_code, String icao_code,
			Double longitude, Double latitude, Double altitude, Double utc_offset, String areaTimezone) {
		super();
		this.airport_name = airportName;
		this.airport_location = airportLocation;
		this.country = country;
		this.longitude = longitude;
		this.latitude = latitude;
		this.area_timezone = areaTimezone;
		this.altitude = altitude;
		this.utc_offset = utc_offset;
		this.iata_code = iata_code;
		this.icao_code = icao_code;
	}

	public String getAirport_name() {
		return airport_name;
	}

	public void setAirport_name(String airportName) {
		this.airport_name = airportName;
	}

	public String getAirport_location() {
		return airport_location;
	}

	public void setAirport_location(String airportLocation) {
		this.airport_location = airportLocation;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public String getArea_timezone() {
		return area_timezone;
	}

	public void setArea_timezone(String areaTimezone) {
		this.area_timezone = areaTimezone;
	}

	public String getIata_code() {
		return iata_code;
	}

	public void setIata_code(String iata_code) {
		this.iata_code = iata_code;
	}

	public String getIcao_code() {
		return icao_code;
	}

	public void setIcao_code(String icao_code) {
		this.icao_code = icao_code;
	}

	public Double getAltitude() {
		return altitude;
	}

	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	public Double getUtc_offset() {
		return utc_offset;
	}

	public void setUtc_offset(Double utc_offset) {
		this.utc_offset = utc_offset;
	}
}
