package frequentFlyer.flight_diary.models.complexModelBuilders;

import frequentFlyer.flight_diary.models.Flight;
import frequentFlyer.flight_diary.models.cacheModels.FlightData;


/**
 * 
 * Complex builder of two independent models
 * 
 * @author Sasa Radovanovic
 *
 */
public class FlightExtended {
	
	private Flight flight;
	private FlightData flightData;
	
	public FlightExtended(Flight flight, FlightData flightData) {
		super();
		this.flight = flight;
		this.flightData = flightData;
	}

	public Flight getFlight() {
		return flight;
	}

	public void setFlight(Flight flight) {
		this.flight = flight;
	}

	public FlightData getFlightData() {
		return flightData;
	}

	public void setFlightData(FlightData flightData) {
		this.flightData = flightData;
	}
	
	
	
	

}
