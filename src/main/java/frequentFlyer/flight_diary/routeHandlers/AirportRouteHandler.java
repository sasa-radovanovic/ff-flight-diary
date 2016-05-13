package frequentFlyer.flight_diary.routeHandlers;

import io.vertx.core.json.Json;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;
import frequentFlyer.flight_diary.repos.AirportRepo;
import frequentFlyer.flight_diary.routeHandlers.errorHandling.ErrorResponseFactory;


/**
 * 
 * Airport Route Handler
 * 
 * This module handles all calls to airport API and delegates execution to airport repo
 * 
 * @author Sasa Radovanovic
 *
 */
public class AirportRouteHandler {
	
	private AirportRepo airportRepo;
	
	public AirportRouteHandler () {
		this.airportRepo = new AirportRepo();
	}

	/**
	 * 
	 * Handle Partial search of airports
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handlePartialSearch(JDBCClient jdbc,
			RoutingContext routingContext) {
		
		
		String criteria = routingContext.request().getParam("criteria");
		
		if (criteria == null || criteria.length() < 2) {
			ErrorResponseFactory.sendError(404, "Criteria missing for airport search", routingContext.response());
		}
		
		System.out.println(this.getClass().getSimpleName() + " | Partial airport search " + criteria);
		
		airportRepo.getPartialSearch(criteria, jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(500, "Airport repo - partial search failed", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(result.result().encode());
			}
		});
		
	}

	/**
	 * 
	 * Handle GET of airport
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void getAirport(JDBCClient jdbc, RoutingContext routingContext) {
		String code = routingContext.request().getParam("code").toUpperCase();
		
		if (code == null || code.length() < 3) {
			ErrorResponseFactory.sendError(404, "Airport code is missing", routingContext.response());
		}
		
		System.out.println(this.getClass().getSimpleName() + " | Get airport data " + code);
		
		airportRepo.getAirport(code, jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(404, "Airport repo - no airport found", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(result.result()));
			}
		});
		
	}

	
	/**
	 * 
	 * Handle GET of airport stats
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void getAirportStats(JDBCClient jdbc, RoutingContext routingContext) {
		airportRepo.getAirportStats(jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(404, "Stats are not ready yet", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(result.result()));
			}
		});
		
	}

	/**
	 * 
	 * Handle GET of airport stats for single airport
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void getSingleAirportDetailed(JDBCClient jdbc,
			RoutingContext routingContext) {
		String code = routingContext.request().getParam("code").toUpperCase();
		
		if (code == null || code.length() < 3) {
			ErrorResponseFactory.sendError(404, "Airport code is missing", routingContext.response());
		}
		
		System.out.println(this.getClass().getSimpleName() + " | Get single airport stats " + code);
		
		airportRepo.getDetailedAirportStats(code, jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(404, "Stats are not ready yet", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(result.result()));
			}
		});
		
	}

}
