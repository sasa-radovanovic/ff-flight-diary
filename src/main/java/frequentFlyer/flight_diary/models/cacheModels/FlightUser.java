package frequentFlyer.flight_diary.models.cacheModels;

/**
 * 
 * Model for cache
 * 
 * @author Sasa Radovanovic
 *
 */
public class FlightUser {

	private String username;
	
	private String timeAdded;
	
	private String mail;
	
	public FlightUser() {
		super();
	}

	public FlightUser(String username, String mail, String timeAdded) {
		super();
		this.username = username;
		this.mail = mail;
		this.timeAdded = timeAdded;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTimeAdded() {
		return timeAdded;
	}

	public void setTimeAdded(String timeAdded) {
		this.timeAdded = timeAdded;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}
	
	
}
