package frequentFlyer.flight_diary.routeHandlers;

import io.vertx.core.json.Json;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;
import frequentFlyer.flight_diary.mailer.ApplicationMailer;
import frequentFlyer.flight_diary.models.User;
import frequentFlyer.flight_diary.repos.UsersRepo;
import frequentFlyer.flight_diary.routeHandlers.errorHandling.ErrorResponseFactory;


/**
 * 
 * User Route Handler
 * 
 * This module handles all calls to user API and delegates execution to user repo
 * 
 * @author Sasa Radovanovic
 *
 */
public class UserRouteHandler {
	
	
	private UsersRepo usersRepo;
	
	public UserRouteHandler (ApplicationMailer mailer) {
		this.usersRepo = new UsersRepo(mailer);
	}
	
	
	/**
	 * 
	 * Handle GET on users (Retrieve all users)
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handleGetUsers (JDBCClient jdbc, RoutingContext routingContext) {
		usersRepo.getAllRows(jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(500, "User repo - Failed to retrieve all users", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(result.result().encode());
			}
		});
	}

	/**
	 * 
	 * Handle DELETE on users (Remove user)
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handleDeleteUser (JDBCClient jdbc, RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			ErrorResponseFactory.sendError(401, "Missing input id for deleting a user", routingContext.response());
		} else {
			try {
				int idToRemove = Integer.parseInt(id);

				usersRepo.deleteUser(idToRemove, jdbc, result -> {
					if (result == null || result.failed()) {
						ErrorResponseFactory.sendError(500, null, routingContext.response());
					} else {
						if (result.succeeded() && result.result() == true) {
							routingContext.response().setStatusCode(204).end();
						} else {
							ErrorResponseFactory.sendError(500, null, routingContext.response());
						}
					}
				});

			} catch (NumberFormatException e) {
				ErrorResponseFactory.sendError(500, "NumberFormatException in deleting account", routingContext.response());
			}
		}
	}

	/**
	 * 
	 * Handle POST on users (Create user)
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handlePostUser (JDBCClient jdbc, RoutingContext routingContext) {
		System.out.println(this.getClass().getSimpleName() + " | Handling account creation...");

		final User user = Json.decodeValue(routingContext.getBodyAsString(), User.class);
		usersRepo.addUser(user, jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(500, "Failed to create new user", routingContext.response());
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(result.result()));
			}
		});
	}


	/**
	 * 
	 * Handle POST on users activation (Activate a user based on token provided)
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handleActivateAccount (JDBCClient jdbc, RoutingContext routingContext) {
		System.out.println(this.getClass().getSimpleName() + " | Handling account activation...");

		String token = routingContext.request().getParam("token");

		System.out.println(this.getClass().getSimpleName() + " | token " + token);

		if (token == null) {
			ErrorResponseFactory.sendError(401, "Missing token for activating user", routingContext.response());
		} else {

			usersRepo.activateUser(token, jdbc, result -> {
				if (result == null || result.failed()) {
					ErrorResponseFactory.sendError(500, "User Repo - Failed to activate user", routingContext.response());
				} else {
					if (result.succeeded() && result.result() != null) {
						routingContext.response().setStatusCode(200).end(Json.encodePrettily(result.result()));
					} else {
						ErrorResponseFactory.sendError(500, "User Repo - Failed to activate user", routingContext.response());
					}
				}
			});

		}

	}

	/**
	 * 
	 * Handle GET of users with the most flights
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void getTopUsers(JDBCClient jdbc, RoutingContext routingContext) {
		usersRepo.getTopUsers(jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(500, "User Repo - Failed to activate user", routingContext.response());
			} else {
				System.out.println("Received top users");
				routingContext.response().setStatusCode(200).end(Json.encodePrettily(result.result()));
			}
		});
		
	}

	/**
	 * 
	 * Handle GET of users count
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handleGetUsersCount(JDBCClient jdbc,
			RoutingContext routingContext) {
		usersRepo.getCount(jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(500, "User Repo - Failed to get count", routingContext.response());
			} else {
				routingContext.response().setStatusCode(200).end(Json.encodePrettily(result.result()));
			}
		});
		
	}

	/**
	 * 
	 * Handle partical search by username
	 * 
	 * @param jdbc - JDBC to create SQL connection
	 * @param routingContext - routingContext containing request and response
	 */
	public void handlePartialSearch(JDBCClient jdbc,
			RoutingContext routingContext) {
		String criteria = routingContext.request().getParam("criteria");
		
		if (criteria == null || criteria.length() < 2) {
			ErrorResponseFactory.sendError(404, "Criteria missing for user search", routingContext.response());
		}
		
		System.out.println(this.getClass().getSimpleName() + " | Partial user search " + criteria);
		
		usersRepo.getPartialSearch (criteria, jdbc, result -> {
			if (result == null || result.failed()) {
				ErrorResponseFactory.sendError(500, "User Repo - Failed to activate user", routingContext.response());
			} else {
				routingContext.response().setStatusCode(200).end(Json.encodePrettily(result.result()));
			}
		});
		
	}
	
}
