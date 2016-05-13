package frequentFlyer.flight_diary.routeHandlers;

import io.vertx.core.json.Json;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;
import frequentFlyer.flight_diary.mailer.ApplicationMailer;
import frequentFlyer.flight_diary.models.Flight;
import frequentFlyer.flight_diary.repos.FlightRepo;
import frequentFlyer.flight_diary.repos.UsersRepo;
import frequentFlyer.flight_diary.routeHandlers.errorHandling.ErrorResponseFactory;



/**
 * 
 * Flight Route Handler
 * 
 * This module handles all calls to flight API and delegates execution to flight repo
 * 
 * @author Sasa Radovanovic
 *
 */
public class FlightRouteHandler {

	private FlightRepo flightsRepo;

	private UsersRepo usersRepo;

	public FlightRouteHandler (ApplicationMailer mailer) {
		this.flightsRepo = new FlightRepo(mailer);
		this.usersRepo = new UsersRepo(mailer);
	}


	/**
	 * 
	 * Handle GET on number of flights in database
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handleFlightsLength (JDBCClient jdbc, RoutingContext routingContext) {
		System.out.println(this.getClass().getSimpleName() + " | Handling flights length...");

		flightsRepo.getCount(jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(500, "Flights Repo - failed to retrieve table length", routingContext.response());
			} else {
				routingContext.response().setStatusCode(200).end(Json.encodePrettily(result.result()));
			}
		});
	}

	/**
	 * 
	 * Handle GET on all flights
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handleGetFlights (JDBCClient jdbc, RoutingContext routingContext) {

		System.out.println(this.getClass().getSimpleName() + " | Handling get flights...");

		flightsRepo.getAllRows(jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(500, "Flights Repo - Failed to retrieve all flights", routingContext.response());
			} else {
				routingContext.response().setStatusCode(200).end(Json.encodePrettily(result.result()));
			}
		});

	}

	/**
	 * 
	 * Handle POST on flights (Create a flight)
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handlePostFlight (JDBCClient jdbc, RoutingContext routingContext) {
		System.out.println(this.getClass().getSimpleName() + " | Handling flight creation...");

		String username;

		if (routingContext.user() == null || routingContext.user().principal() == null || routingContext.user().principal().getString("username") == null) {
			System.out.println(this.getClass().getSimpleName() + " | User is null");
			ErrorResponseFactory.sendError(401, null, routingContext.response());
			return;
		} else {
			username = routingContext.user().principal().getString("username");
		}

		final Flight flight = Json.decodeValue(routingContext.getBodyAsString(), Flight.class);

		usersRepo.getUserByUsername(username, jdbc, user -> {
			if (user == null || user.failed()) {
				ErrorResponseFactory.sendError(404, "User Repo - Failed to retrieve user profile", routingContext.response());
			} else {
				System.out.println("Now add a flight " + username);
				flightsRepo.addFlight(flight, user.result().getUsername(), user.result().getMail(), user.result().getId(), jdbc, addResult -> {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(addResult.result()));
				});
			}
		});
	}

	
	/**
	 * 
	 * Handle GET on user's flights based on credentials of the session
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handleGetMyFlights (JDBCClient jdbc, RoutingContext routingContext) {


		if (routingContext.user() == null) {
			System.out.println(this.getClass().getSimpleName() + " | User is null");
			ErrorResponseFactory.sendError(401, null, routingContext.response());
		} else {
			System.out.println(this.getClass().getSimpleName() + " | " + routingContext.user().principal().getString("username"));
		}

		flightsRepo.getMyFlights(routingContext.user().principal().getString("username"), jdbc, result -> {
			if (result == null) {
				ErrorResponseFactory.sendError(500, "Flight Repo - Retrieve user flights failed", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(result));
			}
		});

	}


	/**
	 * 
	 * Handle GET on user's flights
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handleGetUsersFlights (JDBCClient jdbc, RoutingContext routingContext) {

		String username = routingContext.request().getParam("username");
		
		if (username == null || username.equalsIgnoreCase("")) {
			ErrorResponseFactory.sendError(500, "Flight Repo - Username is missing", routingContext.response());
		}

		flightsRepo.getMyFlights(username, jdbc, result -> {
			if (result == null) {
				ErrorResponseFactory.sendError(500, "Flight Repo - Retrieve user flights failed", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(result));
			}
		});
	}


	/**
	 * 
	 * Handle DELETE of flight based on id
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handleDeleteFlight (JDBCClient jdbc, RoutingContext routingContext) {

		String id = routingContext.request().getParam("flight");

		if (id == null) {
			ErrorResponseFactory.sendError(401, "Missing input id for deleting a flight", routingContext.response());
		} else {
			try {
				int idToRemove = Integer.parseInt(id);
				String username = null;

				if (routingContext.user() == null || routingContext.user().principal() == null || routingContext.user().principal().getString("username") == null) {
					ErrorResponseFactory.sendError(401, "You are not authorized for this operation", routingContext.response());
				} else {
					username = routingContext.user().principal().getString("username");
				}

				flightsRepo.removeFlight(idToRemove, username, jdbc, result -> {
					routingContext.response().setStatusCode(204).end();
				});

			} catch (NumberFormatException e) {
				ErrorResponseFactory.sendError(500, "NumberFormatException in deleting account", routingContext.response());
			}
		}
	}

	/**
	 * 
	 * Handle PATCH on flight (Edit flight)
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handleEditFlight(JDBCClient jdbc, RoutingContext routingContext) {
		System.out.println(this.getClass().getSimpleName() + " | Handling edit flight...");

		String username;

		if (routingContext.user() == null || routingContext.user().principal() == null || routingContext.user().principal().getString("username") == null) {
			System.out.println(this.getClass().getSimpleName() + " | User is null");
			ErrorResponseFactory.sendError(401, null, routingContext.response());
			return;
		} else {
			username = routingContext.user().principal().getString("username");
		}

		final Flight flight = Json.decodeValue(routingContext.getBodyAsString(), Flight.class);

		flightsRepo.editFlight(flight, username, jdbc, handler -> {
			routingContext.response().setStatusCode(204).end();
		});
		
	}
	
	/**
	 * 
	 * Handle GET on user statistics based on flights
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handleGetUserStatistics (JDBCClient jdbc, RoutingContext routingContext) {
		
		String username;

		if (routingContext.user() == null || routingContext.user().principal() == null || routingContext.user().principal().getString("username") == null) {
			System.out.println(this.getClass().getSimpleName() + " | User is null");
			ErrorResponseFactory.sendError(401, null, routingContext.response());
			return;
		} else {
			username = routingContext.user().principal().getString("username");
		}
		
		System.out.println(this.getClass().getSimpleName() + " | Handling statistics for user " + username);

		flightsRepo.getUsersStats(jdbc, username, userStatsHandler -> {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(userStatsHandler.result()));
		});
		
	}

}
