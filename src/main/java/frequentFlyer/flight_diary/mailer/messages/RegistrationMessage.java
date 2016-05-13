package frequentFlyer.flight_diary.mailer.messages;


/**
 * 
 * RegistrationMessage
 * 
 * @author Sasa Radovanovic
 *
 */
public class RegistrationMessage implements MailContent {

	
	private String title;
	
	private String body;
	
	public RegistrationMessage (String token, String url) {
		title = "Welcome to your own Flight Diary!";
		body = "In order to activate your account please follow the <a href='" + url + "#/activate/" + token + "/'> link</a>.</br> ";
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
