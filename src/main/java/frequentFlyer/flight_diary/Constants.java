package frequentFlyer.flight_diary;


public class Constants {
	
	public static final String REGISTRATION = "registration";
	
	public static final String REGISTRATION_TOKEN_KEY = "token";
	
	public static final String DEPLOYMENT_URL_KEY = "public_url";
	
	public static String DEPLOYMENT_URL = "http://localhost:8080";
	
	//Purpose constants
	public static final int PURPOSE_BUSINESS = 1;
	public static final int PURPOSE_LEASURE  = 2;
	public static final int PURPOSE_FAMILY_VISIT = 3;
	
	//Error constants
	public static final String ERROR_REASON = "error_msg";
	
	public static final String ERROR_401_MSG = "You are unauthorized";
	public static final String ERROR_404_MSG = "Not found";
	public static final String ERROR_409_MSG = "Conflict";
	public static final String ERROR_500_MSG = "Internal server error occured";
	
	public static final String MORNING_FLIGHTS = "morning";
	public static final String AFTERNOON_FLIGHTS = "afternoon";
	public static final String EVENING_FLIGHTS = "evening";
	
	public static final String FLOWN_TO = "flown_to";
	public static final String FLOWN_FROM = "flown_from";
	

}
