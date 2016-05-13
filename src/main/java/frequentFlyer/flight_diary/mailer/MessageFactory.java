package frequentFlyer.flight_diary.mailer;

import io.vertx.core.json.JsonObject;
import frequentFlyer.flight_diary.Constants;
import frequentFlyer.flight_diary.mailer.messages.MailContent;
import frequentFlyer.flight_diary.mailer.messages.RegistrationMessage;

/**
 * 
 * Factory pattern of Message content
 * 
 * @author Sasa Radovanovic
 *
 */
public class MessageFactory {


	public static MailContent getMailContent (String mailType, JsonObject mailConfig) {
		if (mailType == null){
			return null;
		} else if (mailType.equalsIgnoreCase(Constants.REGISTRATION)) {
			return new RegistrationMessage(mailConfig.getString(Constants.REGISTRATION_TOKEN_KEY), mailConfig.getString(Constants.DEPLOYMENT_URL_KEY));
		} else {
			return null;
		}	

	}


}
