package frequentFlyer.flight_diary;

import frequentFlyer.flight_diary.auth.ApplicationAuthProvider;
import frequentFlyer.flight_diary.mailer.ApplicationMailer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.mail.StartTLSOptions;


/**
 * FLIGHT DIARY APPLICATION
 * 
 * This code is free to change and distribute and is under no licence
 * 
 * @author Sasa Radovanovic
 *
 */
public class FlightDiaryApplication extends AbstractVerticle {


	// This provides support for running application from your IDE
	public static void main(String[] args) {
		Runner.runExample(FlightDiaryApplication.class);
	}

	@Override
	public void start(Future<Void> fut) {
		
		// Use cleanDatabase and fillWithFixtures together - both true or false
		// If set to true, database (flights and users tables) will be cleaned on start-up 
		boolean cleanDatabase = false;
		
		// If set to true, database (flights and users tables) will be filled with fixtures
		boolean fillWithFixtures = false;
		
		// Database settings. If you want to run it against other database, be sure to change pom.xml
		String databaseJDBCUrl = "jdbc:postgresql://localhost:5432/flight_vertx";
		String databaseUsername = "postgres";
		String databasePassword = "insight";
		String databaseDriver = "org.postgresql.Driver";
		
		// CSV files from which database gets filled. Theses files arrived with the application
		String airportsLocation = "C:\\Users\\rsasa\\Desktop\\airports.csv"; 
		String airlinesLocation = "C:\\Users\\rsasa\\Desktop\\airlines.csv";
		String airplaneTypesLocation = "C:\\Users\\rsasa\\Desktop\\airplanes.csv";
		
		// Mail settings. You can leave this for testing. I opened this mail in that purpose - it's your's free to use.
		String email = "flight.diary@yandex.com";
		String smtpLoc = "smtp.yandex.com";
		int smtpPort = 587;
		String emailUsername = "flight.diary@yandex.com";
		String emailPassword = "frequentflyer";
		
		// Deployment port. If set to 8080, your application will be available at http://localhost:8080
		int deploymentPort = 8080;
		// Set your deployment address - this is used when sending link inside the mail message
		Constants.DEPLOYMENT_URL = "http://localhost:8080";
		
		// START OF INITIALIZATION
		// Initialize database connection
		DatabaseConnector.getInstance().setUpDatabase(vertx, databaseJDBCUrl, 
				databaseUsername, databasePassword, databaseDriver, jdbc -> {

					System.out.println(this.getClass().getSimpleName() + " | Database set...");

					// Initialize data. Creation, deletion and fill of tables is done by this module
					InitialDataFiller.getInstance().prefillData(cleanDatabase, fillWithFixtures, airportsLocation, airlinesLocation, airplaneTypesLocation, 
							jdbc, ready -> {
								
						System.out.println(this.getClass().getSimpleName() + " | Prefilled data...");
						
						// Initialize auth provider. Logging and access to secure resources is controlled by this module
						ApplicationAuthProvider.getInstance().setJDBCAuth(jdbc, authProvider -> {
							
							// Initialize mailer
							ApplicationMailer.getInstance().initialize(vertx, email, smtpLoc, smtpPort, StartTLSOptions.REQUIRED, emailUsername, emailPassword, mailer -> {
								if (mailer != null) {

									// Initialize router
									ApplicationRouter.getInstance().initialize(vertx, authProvider, mailer, preparedRouter -> {
										if (preparedRouter != null) {

											System.out.println(this.getClass().getSimpleName() + " | Router initialized...");

											// START THE SERVER
											vertx
											.createHttpServer()
											.requestHandler(preparedRouter::accept)
											.listen(
													config().getInteger("http.port", deploymentPort),
													result -> {
														if (result.succeeded()) {
															fut.complete();
														} else {
															fut.fail(result.cause());
														}
													}
													);

										} else {
											System.err.println(this.getClass().getSimpleName() + " | Routes not initialized. Check your stack trace for errors...");
										}

									});
								} else {
									System.err.println(this.getClass().getSimpleName() + " | Mailer not initialized. Check your stack trace for errors...");
								}
							});

						});		
					});
				});
	}



}
