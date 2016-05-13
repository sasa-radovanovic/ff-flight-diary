package frequentFlyer.flight_diary;

import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;

import frequentFlyer.flight_diary.mailer.ApplicationMailer;
import frequentFlyer.flight_diary.mailer.messages.SameRouteAddedMessage;
import frequentFlyer.flight_diary.models.Airport;
import frequentFlyer.flight_diary.models.cacheModels.FlightData;
import frequentFlyer.flight_diary.models.cacheModels.FlightIdentifier;
import frequentFlyer.flight_diary.models.cacheModels.FlightUser;
import frequentFlyer.flight_diary.utils.FlightDataCalculator;
import frequentFlyer.flight_diary.utils.FlightDateFormatter;

/**
 * 
 * Application Cache Manager
 * 
 * NOTE: Cache is not persisent on disk and is not restarted on application restart - but reinitialized
 * Basically - Cache is used a quick-access, write through data structure in front of Database
 * 
 * @author Sasa Radovanovic
 *
 */
public class ApplicationCacheManager {

	private CacheManager cacheManager;
	private Cache<FlightIdentifier, FlightData> flightCache;
	private ApplicationMailer mailer;

	/**
	 * @param mailer - Mailer which is user for sending e-mail when another user adds flight
	 * @param handler - Callback on job done
	 */
	public ApplicationCacheManager (ApplicationMailer mailer, Handler<Void> handler) {
		if (this.cacheManager == null) {

			this.mailer = mailer;

			cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

			flightCache = cacheManager.createCache("flightCache",
					CacheConfigurationBuilder.newCacheConfigurationBuilder(FlightIdentifier.class, FlightData.class).build());
		}
		handler.handle(null);
	}



	/**
	 * 
	 * If Flight data for certain route exists, return it, if not create a cache entry
	 * 
	 * @param airportFrom - IATA code of departure airport
	 * @param airportTo - IATA code of arrival airport
	 * @param username - username of user who adds a flight
	 * @param mail - mail of user who adds a flight
	 * @param added - if this function is called on creating a flight
	 * @return FlightData object from models package
	 */
	public FlightData getFlightDataOrCreate (Airport airportFrom, Airport airportTo, String username, String mail, boolean added) {
		FlightIdentifier flightIdent = new FlightIdentifier(airportFrom.getIata_code(), airportTo.getIata_code());
		if (flightCache.containsKey(flightIdent)) {
			if (added) {
				FlightData oldFlightData = flightCache.get(flightIdent);
				// JAVA 8 Feature :)
				List<FlightUser> userList = oldFlightData.getUsersRecentlyAdded().stream()
						.filter((flightUser) -> flightUser.getUsername().equals((username)))
						.collect(Collectors.toList());
				if (userList == null || userList.size() == 0) {
					oldFlightData.getUsersRecentlyAdded().add(new FlightUser(username, mail, FlightDateFormatter.formatDate(new Date())));
					flightCache.replace(flightIdent, oldFlightData);
					ArrayList<String> sendNotificationList = new ArrayList<String>();
					for (FlightUser flightUserInCache : oldFlightData.getUsersRecentlyAdded()) {
						sendNotificationList.add(flightUserInCache.getMail());
					}
					sendNotificationToOthers(sendNotificationList, mail, airportFrom.getIata_code(), airportTo.getIata_code(), username);
				} 
			}
			return flightCache.get(flightIdent);
		} else {
			ArrayList<FlightUser> users = new ArrayList<FlightUser>();
			users.add(new FlightUser(username, mail, FlightDateFormatter.formatDate(new Date())));
			FlightData flightData = new FlightData(FlightDataCalculator.calculateDistance(airportFrom.getLatitude(), 
					airportFrom.getLongitude(), airportTo.getLatitude(), airportFrom.getLongitude()), users, 
					airportFrom.getLatitude(), airportFrom.getLongitude(), airportTo.getLatitude(), airportTo.getLongitude());
			flightCache.put(flightIdent, flightData);
			return flightData;
		}
	}
	
	public FlightData getFlightDataByCodes (String codeAirportFrom, String codeAirportTo) {
		FlightIdentifier flightIdent = new FlightIdentifier(codeAirportFrom, codeAirportTo);
		if (!flightCache.containsKey(flightIdent)) {
			return null;
		}
		return flightCache.get(flightIdent);
	}


	private void sendNotificationToOthers (ArrayList<String> existingUsersMails, String addingUserMail, String airportFromIATA, String airportToIATA, String username) {
		if (existingUsersMails != null && existingUsersMails.size() > 0) {
			SameRouteAddedMessage message = new SameRouteAddedMessage(Constants.DEPLOYMENT_URL, airportFromIATA, airportToIATA, username);
			// JAVA 8 Feature :)
			List<String> toSendList = existingUsersMails.stream()
					.filter((mailToS) -> !mailToS.equals((addingUserMail)))
					.collect(Collectors.toList());
			if (toSendList != null && toSendList.size() > 0) {
				mailer.sendBulk(toSendList, message, handler -> {
					System.out.println(this.getClass().getSimpleName() + " | Send notification about added flight");
				});
			}
		}	
	}
}
