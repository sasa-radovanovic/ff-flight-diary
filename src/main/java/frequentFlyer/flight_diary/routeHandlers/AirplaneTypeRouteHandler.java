package frequentFlyer.flight_diary.routeHandlers;

import io.vertx.core.json.Json;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;
import frequentFlyer.flight_diary.repos.AirplaneTypesRepo;
import frequentFlyer.flight_diary.routeHandlers.errorHandling.ErrorResponseFactory;

/**
 * 
 * Airplane Type Route Handler
 * 
 * This module handles all calls to airplane types API and delegates execution to airplane types repo
 * 
 * @author Sasa Radovanovic
 *
 */
public class AirplaneTypeRouteHandler {

	
	private AirplaneTypesRepo airplaneTypeRepo;
	
	public AirplaneTypeRouteHandler () {
		this.airplaneTypeRepo = new AirplaneTypesRepo();
	}

	/**
	 * 
	 * Handle Partial search of airplane types
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handlePartialSearch(JDBCClient jdbc,
			RoutingContext routingContext) {
		String criteria = routingContext.request().getParam("criteria");

		if (criteria == null || criteria.length() < 2) {
			ErrorResponseFactory.sendError(404, "Missing input criteria for partial search", routingContext.response());
		}

		airplaneTypeRepo.getPartialSearch(criteria, jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(500, "Airline Type Repo - partial search failed", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(result.result().encode());
			}
		});
		
	}

	/**
	 * 
	 * Handle GET of airplane type
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void getAirplaneType(JDBCClient jdbc, RoutingContext routingContext) {
		String code = routingContext.request().getParam("code").toUpperCase();
		
		if (code == null || code.length() < 3) {
			ErrorResponseFactory.sendError(404, "Airplane Type IATA code is missing", routingContext.response());
		}
		
		System.out.println(this.getClass().getSimpleName() + " | Get airplane type data " + code);
		
		airplaneTypeRepo.getAirplaneType(code, jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(404, "Airplane Type Repo - no airline found", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(result.result()));
			}
		});
		
	}

	/**
	 * 
	 * Handle GET of airplane type detailed stats
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void getSingleAirplaneTypeDetailedStats(JDBCClient jdbc,
			RoutingContext routingContext) {
		String code = routingContext.request().getParam("code").toUpperCase();
		
		if (code == null || code.length() < 3) {
			ErrorResponseFactory.sendError(404, "Airplane Type IATA code is missing", routingContext.response());
		}
		
		System.out.println(this.getClass().getSimpleName() + " | Get airplane type stats " + code);
		
		airplaneTypeRepo.getAirplaneTypeStats(code, jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(404, "Airplane Type Repo - no stats found", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(result.result()));
			}
		});
		
	}
	
}
