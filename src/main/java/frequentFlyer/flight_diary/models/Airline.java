package frequentFlyer.flight_diary.models;

import io.vertx.core.json.JsonObject;

/**
 * @author Sasa Radovanovic
 *
 */
public class Airline {
	
	String airline_name;
	
	String airline_iata_code;
	
	String airline_icao_code;
	
	String airline_country;
	
	String airline_callsign;
	

	public Airline() {
		super();
	}
	
	public Airline(JsonObject jsonObject) {
		if (jsonObject.containsKey("airline_name")) {
			this.airline_name = jsonObject.getString("airline_name");
		}
		if (jsonObject.containsKey("airline_iata_code")) {
			this.airline_iata_code = jsonObject.getString("airline_iata_code");
		}
		if (jsonObject.containsKey("airline_icao_code")) {
			this.airline_icao_code = jsonObject.getString("airline_icao_code");
		}
		if (jsonObject.containsKey("airline_country")) {
			this.airline_country = jsonObject.getString("airline_country");
		}
		if (jsonObject.containsKey("airline_callsign")) {
			this.airline_callsign = jsonObject.getString("airline_callsign");
		}
	}

	public Airline(String airline_name, String airline_iata_code,
			String airline_icao_code, String airline_country,
			String airline_callsign) {
		super();
		this.airline_name = airline_name;
		this.airline_iata_code = airline_iata_code;
		this.airline_icao_code = airline_icao_code;
		this.airline_country = airline_country;
		this.airline_callsign = airline_callsign;
	}

	public String getAirline_name() {
		return airline_name;
	}

	public void setAirline_name(String airline_name) {
		this.airline_name = airline_name;
	}

	public String getAirline_iata_code() {
		return airline_iata_code;
	}

	public void setAirline_iata_code(String airline_iata_code) {
		this.airline_iata_code = airline_iata_code;
	}

	public String getAirline_icao_code() {
		return airline_icao_code;
	}

	public void setAirline_icao_code(String airline_icao_code) {
		this.airline_icao_code = airline_icao_code;
	}

	public String getAirline_country() {
		return airline_country;
	}

	public void setAirline_country(String airline_country) {
		this.airline_country = airline_country;
	}

	public String getAirline_callsign() {
		return airline_callsign;
	}

	public void setAirline_callsign(String airline_callsign) {
		this.airline_callsign = airline_callsign;
	}
	
	
	
	
	

}
