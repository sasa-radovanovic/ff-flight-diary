package frequentFlyer.flight_diary.mailer;

import frequentFlyer.flight_diary.mailer.messages.MailContent;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * Application Mailer
 * 
 * This module presents concrete implementation of Vertx mailer with the data provided at start-up
 * 
 * @author Sasa Radovanovic
 *
 */
public class ApplicationMailer {

	private static ApplicationMailer applicationMailer = new ApplicationMailer();

	private MailClient mailClient;
	private String senderAddress;

	private ApplicationMailer () {
	}

	public static ApplicationMailer getInstance( ) {
		return applicationMailer;
	}
	

	public void initialize (Vertx vertx, String senderAddress, String hostname, int port, StartTLSOptions tlsOption, String username, String password, Handler<ApplicationMailer> handler) {
		if (mailClient == null) {
			this.senderAddress = senderAddress;
			MailConfig config = new MailConfig();
			config.setHostname(hostname);
			config.setPort(port);
			config.setStarttls(tlsOption);
			config.setUsername(username);
			config.setPassword(password);
			mailClient = MailClient.createNonShared(vertx, config);
			handler.handle(applicationMailer);
		} else {
			handler.handle(null);
		}
	}

	/**
	 * 
	 * Send single mail
	 * 
	 * @param addresses - Whom to send?
	 * @param content - What to send?
	 * @param handler - Callback on job done.
	 */
	public void sendMail (String address, MailContent content, Handler<Boolean> handler) {
		MailMessage message = new MailMessage();
		message.setFrom(senderAddress);
		message.setTo(address);
		message.setSubject(content.getTitle());
		message.setHtml(content.getBody());
		mailClient.sendMail(message, mailResult -> {
			if (mailResult.succeeded()) {
				handler.handle(true);
			} else {
				handler.handle(false);
			}
		});
	}


	/**
	 * 
	 * Send bulk msg's. This is not used at the momment 
	 * 
	 * @param addresses - Whom to send?
	 * @param content - What to send?
	 * @param handler - Callback on job done.
	 */
	public void sendBulk (List<String> addresses, MailContent content, Handler<ArrayList<String>> handler) {
		ArrayList<String> sentList = new ArrayList<String>();
		for (String singleAddress : addresses) {
			sendMail(singleAddress, content, singleHandler -> {
				if (singleHandler && singleHandler.booleanValue()) {
					sentList.add(singleAddress);
				}
			});
		}
		handler.handle(sentList);
	}

}
