package frequentFlyer.flight_diary.routeHandlers;

import io.vertx.core.json.Json;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;
import frequentFlyer.flight_diary.repos.AirlineRepo;
import frequentFlyer.flight_diary.routeHandlers.errorHandling.ErrorResponseFactory;

/**
 * 
 * Airline Route Handler
 * 
 * This module handles all calls to airlines API and delegates execution to airline repo
 * 
 * @author Sasa Radovanovic
 *
 */
public class AirlineRouteHandler {
	
	private AirlineRepo airlineRepo;
	
	public AirlineRouteHandler () {
		this.airlineRepo = new AirlineRepo();
	}

	/**
	 * 
	 * Handle Partial search of airlines
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handlePartialSearch(JDBCClient jdbc,
			RoutingContext routingContext) {
		String criteria = routingContext.request().getParam("criteria");

		if (criteria == null || criteria.length() < 2) {
			ErrorResponseFactory.sendError(404, "Missing input criteria for partial search",routingContext.response());
		}

		airlineRepo.getPartialSearch(criteria, jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(500, "Airline Repo - partial search failed", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(result.result().encode());
			}
		});
		
	}

	/**
	 * 
	 * Handle GET of airline
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void getAirline(JDBCClient jdbc, RoutingContext routingContext) {
		String code = routingContext.request().getParam("code").toUpperCase();
		
		if (code == null || code.length() < 2) {
			ErrorResponseFactory.sendError(404, "Airline IATA code is missing", routingContext.response());
		}
		
		System.out.println(this.getClass().getSimpleName() + " | Get airline data " + code);
		
		airlineRepo.getAirline(code, jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(404, "Airline repo - no airline found", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(result.result()));
			}
		});
		
	}

	/**
	 * 
	 * Handle GET of general stats based on entire system
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void getGeneralStats(JDBCClient jdbc, RoutingContext routingContext) {
		airlineRepo.getGeneralStatistics(jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(404, "Airline repo - stats not ready", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(result.result()));
			}
		});
	}

	/**
	 * 
	 * Handle GET of general stats for single airline
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void getSingleAirlineDetailedStats(JDBCClient jdbc,
			RoutingContext routingContext) {
		
		String code = routingContext.request().getParam("code").toUpperCase();
		
		if (code == null || code.length() < 2) {
			ErrorResponseFactory.sendError(404, "Airline IATA code is missing", routingContext.response());
		}
		
		System.out.println(this.getClass().getSimpleName() + " | Get airline stats " + code);
		
		airlineRepo.getSingleAirlineStats(code, jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(404, "Airline repo - stats not ready", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(result.result()));
			}
		});
	}

}
