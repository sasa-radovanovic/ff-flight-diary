package frequentFlyer.flight_diary.models;

import io.vertx.core.json.JsonObject;


/**
 * @author Sasa Radovanovic
 *
 */
public class Flight {

	private int flight_id;
	
	private String departure;
	
	private String arrival;
	
	private String departure_time;
	
	private String airline_code;
	
	private String airplane;
	
	private int rating;
	
	private String ticket_source;
	
	private int purpose;
	
	private int flight_class;
	
	private int user_id;
	
	
	public Flight() {
		super();
	}

	
	public Flight(String departure, String arrival,
			String departure_time, int user_id) {
		super();
		this.departure = departure;
		this.arrival = arrival;
		this.departure_time = departure_time;
		this.user_id = user_id;
	}
	
	public Flight(JsonObject jsonObject) {
		if (jsonObject.containsKey("flight_id")) {
			this.flight_id = jsonObject.getInteger("flight_id");
		}
		if (jsonObject.containsKey("departure")) {
			this.departure = jsonObject.getString("departure");
		}
		if (jsonObject.containsKey("arrival")) {
			this.arrival = jsonObject.getString("arrival");
		}
		if (jsonObject.containsKey("departure_time")) {
			this.departure_time = jsonObject.getString("departure_time");
		}
		if (jsonObject.containsKey("airline_code")) {
			this.airline_code = jsonObject.getString("airline_code");
		}
		if (jsonObject.containsKey("airplane_type")) {
			this.airplane = jsonObject.getString("airplane_type");
		}
		if (jsonObject.containsKey("ticket_source")) {
			this.ticket_source = jsonObject.getString("ticket_source");
		}
		if (jsonObject.containsKey("rating")) {
			this.rating = jsonObject.getInteger("rating");
		}
		if (jsonObject.containsKey("purpose")) {
			this.purpose = jsonObject.getInteger("purpose");
		}
		if (jsonObject.containsKey("flight_class")) {
			this.flight_class = jsonObject.getInteger("flight_class");
		}
		if (jsonObject.containsKey("user_id")) {
			this.user_id = jsonObject.getInteger("user_id");
		}
	}
	
	
	
	public Flight(String departure, String arrival,
			String departure_time, String airline_code, String airplane, int flight_class, int user_id) {
		super();
		this.departure = departure;
		this.arrival = arrival;
		this.departure_time = departure_time;
		this.airline_code = airline_code;
		this.airplane = airplane;
		this.flight_class = flight_class;
		this.user_id = user_id;
	}


	public Flight(int flight_id, String departure, String arrival,
			String departure_time, String airline_code, String airplane, int rating,
			String ticket_source, int purpose, int flight_class, int user_id) {
		super();
		this.flight_id = flight_id;
		this.departure = departure;
		this.arrival = arrival;
		this.departure_time = departure_time;
		this.airline_code = airline_code;
		this.airplane = airplane;
		this.rating = rating;
		this.ticket_source = ticket_source;
		this.flight_class = flight_class;
		this.purpose = purpose;
		this.user_id = user_id;
	}


	public int getFlight_id() {
		return flight_id;
	}


	public void setFlight_id(int flight_id) {
		this.flight_id = flight_id;
	}


	public String getDeparture() {
		return departure;
	}


	public void setDeparture(String departure) {
		this.departure = departure;
	}


	public String getArrival() {
		return arrival;
	}


	public void setArrival(String arrival) {
		this.arrival = arrival;
	}


	public String getDeparture_time() {
		return departure_time;
	}


	public void setDeparture_time(String departure_time) {
		this.departure_time = departure_time;
	}


	public int getRating() {
		return rating;
	}


	public void setRating(int rating) {
		this.rating = rating;
	}


	public String getTicket_source() {
		return ticket_source;
	}


	public void setTicket_source(String ticket_source) {
		this.ticket_source = ticket_source;
	}


	public int getPurpose() {
		return purpose;
	}


	public void setPurpose(int purpose) {
		this.purpose = purpose;
	}


	public int getUser_id() {
		return user_id;
	}


	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}


	public String getAirline_code() {
		return airline_code;
	}


	public void setAirline_code(String airline_code) {
		this.airline_code = airline_code;
	}


	public String getAirplane() {
		return airplane;
	}


	public void setAirplane(String airlane) {
		this.airplane = airlane;
	}


	public int getFlight_class() {
		return flight_class;
	}


	public void setFlight_class(int flight_class) {
		this.flight_class = flight_class;
	}


	@Override
	public String toString() {
		return "Flight [flight_id=" + flight_id + ", departure=" + departure
				+ ", arrival=" + arrival + ", departure_time=" + departure_time
				+ ", airline_code="
				+ airline_code + ", airplane=" + airplane + ", rating="
				+ rating + ", ticket_source=" + ticket_source + ", purpose="
				+ purpose + ", flight_class=" + flight_class + ", user_id="
				+ user_id + "]";
	}
	
	
}
