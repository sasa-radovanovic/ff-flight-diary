package frequentFlyer.flight_diary.mailer.messages;


/**
 * 
 * RegistrationMessage
 * 
 * @author Sasa Radovanovic
 *
 */
public class SameRouteAddedMessage implements MailContent {
	
	
	private String title;
	
	private String body;
	
	
	public SameRouteAddedMessage (String url, String from, String to, String username) {
		title = "Flight Diary members are travelling the same route like you";
		body = "Another member just added a flight from " + from + " to " + to + " on his/her account. Check out <a href='" + url + "/#/profile/" + username + "/'> " + username + "'s profile.</a></br> ";
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getBody() {
		return body;
	}

}
