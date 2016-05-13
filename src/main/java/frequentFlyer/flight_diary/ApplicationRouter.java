package frequentFlyer.flight_diary;

import frequentFlyer.flight_diary.mailer.ApplicationMailer;
import frequentFlyer.flight_diary.routeHandlers.AirlineRouteHandler;
import frequentFlyer.flight_diary.routeHandlers.AirplaneTypeRouteHandler;
import frequentFlyer.flight_diary.routeHandlers.AirportRouteHandler;
import frequentFlyer.flight_diary.routeHandlers.FlightRouteHandler;
import frequentFlyer.flight_diary.routeHandlers.UserRouteHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

/**
 * 
 * Application Router
 * 
 * This module is the core of the system. It is similar to Express in node.js as it 
 * basically sets up routes and entire middleware. 
 * 
 * @author Sasa Radovanovic
 *
 */
public class ApplicationRouter {


	private static ApplicationRouter applicationRouter = new ApplicationRouter();

	private Router router;
	
	// Route Handlers 
	// Every API call is forwarded to specific Route Handler
	private UserRouteHandler userRouteHandler;
	private FlightRouteHandler flightRouteHandler;
	private AirlineRouteHandler airlineRouteHandler;
	private AirportRouteHandler airportRouteHandler;
	private AirplaneTypeRouteHandler airplaneTypeRouteHandler;

	// Singleton pattern
	private ApplicationRouter () {
	}

	public static ApplicationRouter getInstance( ) {
		return applicationRouter;
	}

	
	/**
	 * @param vertx - Vertx object to which router is bind to
	 * @param authProvider - auth provider to user
	 * @param mailer - mailer to use
	 * @param handler - Callback on job done (Middleware set)
	 */
	public void initialize (Vertx vertx, JDBCAuth authProvider, ApplicationMailer mailer, Handler<Router> handler) {
		if (router == null) {
			router = Router.router(vertx);
			userRouteHandler = new UserRouteHandler(mailer);
			flightRouteHandler = new FlightRouteHandler(mailer);
			airportRouteHandler = new AirportRouteHandler();
			airlineRouteHandler = new AirlineRouteHandler();
			airplaneTypeRouteHandler = new AirplaneTypeRouteHandler();
			setUpRouter(vertx, authProvider, localHandle -> {
				handler.handle(router);
			});
		} else {
			handler.handle(null);
		}
	}

	/**
	 * @param vertx - Vertx object to which router is bind to
	 * @param authProvider - auth provider to user
	 * @param handler - Callback on job done (Middleware set)
	 */
	private void setUpRouter (Vertx vertx, JDBCAuth authProvider, Handler<Router> localHandler) {

		System.out.println(this.getClass().getSimpleName() + " | Set up router...");

		// General handlers
		router.route().handler(CookieHandler.create());
		router.route().handler(BodyHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

		System.out.println(this.getClass().getSimpleName() + " | Cookie, Body and Session handlers set...");

		DatabaseConnector.getInstance().getJDBCConnector(jdbc -> {

			System.out.println(this.getClass().getSimpleName() + " | Application authentication provider set...");

			router.route().handler(UserSessionHandler.create(authProvider));

			// Any requests to URI starting '/private/' require login
			router.route("/private/*").handler(RedirectAuthHandler.create(authProvider, "/#/login"));

			// Serve the static private pages from directory 'private'
			router.route("/private/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("private"));

			// Handles the actual login
			router.route("/loginhandler").handler(FormLoginHandler.create(authProvider));

			// Implement logout
			router.route("/logout").handler(context -> {
				context.clearUser();
				// Redirect back to the index page
				context.response().putHeader("location", "/").setStatusCode(302).end();
			});
			
			// Gets username of logged in user
			router.post("/whoami").handler(context -> {
				String username = context.user().principal().getString("username");
				JsonObject respo = new JsonObject();
				respo.put("youare", username);
				context.response().end(respo.encode());
			});
			
			//******************** API ROUTES ************************/

			//API call - Get all users
			router.get("/api/users").handler(routingContext -> {
				userRouteHandler.handleGetUsers(jdbc, routingContext);
			});
			
			//API call - Get all users count
			router.get("/api/users/count").handler(routingContext -> {
				userRouteHandler.handleGetUsersCount(jdbc, routingContext);
			});

			//API call - Create new user
			router.post("/api/users").handler(routingContext -> {
				userRouteHandler.handlePostUser(jdbc, routingContext);
			});

			//API call - Delete a user
			router.delete("/api/users/:id").handler(routingContext -> {
				userRouteHandler.handleDeleteUser(jdbc, routingContext);
			});

			//API call - Activate user
			router.post("/api/activate/:token").handler(routingContext -> {
				userRouteHandler.handleActivateAccount(jdbc, routingContext);
			});


			//API call - Total flights in DB
			router.get("/api/size/flights").handler(routingContext -> {
				flightRouteHandler.handleFlightsLength(jdbc, routingContext);
			});

			//API call - Get all flights
			router.get("/api/all/flights").handler(routingContext -> {
				flightRouteHandler.handleGetFlights(jdbc, routingContext);
			});

			//API call - Create a new flight
			router.post("/api/flights").handler(routingContext -> {
				flightRouteHandler.handlePostFlight(jdbc, routingContext);
			});
			
			//API call - Edit a flight
			router.patch("/api/flights").handler(routingContext -> {
				flightRouteHandler.handleEditFlight(jdbc, routingContext);
			});
			
			//API call - Delete a flight
			router.delete("/api/flights/:flight").handler(routingContext -> {
				flightRouteHandler.handleDeleteFlight(jdbc, routingContext);
			});
			
			//API call - Get logged in user flights
			router.get("/api/flights").handler(routingContext -> {
				flightRouteHandler.handleGetMyFlights (jdbc, routingContext);
			});
			
			//API call - Get users in flights
			router.get("/api/flights/:username").handler(routingContext -> {
				flightRouteHandler.handleGetUsersFlights (jdbc, routingContext);
			});
			
			//API call - get airports by partial search
			router.post("/api/airports/partial/:criteria").handler(routingContext -> {
				airportRouteHandler.handlePartialSearch (jdbc, routingContext);
			});
			
			//API call - get airports by partial search
			router.get("/api/airports/partial/:criteria").handler(routingContext -> {
				airportRouteHandler.handlePartialSearch (jdbc, routingContext);
			});
			
			//API call - get users by partial search
			router.post("/api/users/partial/:criteria").handler(routingContext -> {
				userRouteHandler.handlePartialSearch (jdbc, routingContext);
			});
			
			//API call - get users by partial search
			router.get("/api/users/partial/:criteria").handler(routingContext -> {
				userRouteHandler.handlePartialSearch (jdbc, routingContext);
			});
			
			//API call - get airport data by IATA code
			router.get("/api/airports/:code").handler(routingContext -> {
				airportRouteHandler.getAirport (jdbc, routingContext);
			});
			
			
			//API call - get airports statistic 
			router.get("/api/stats/airports").handler(routingContext -> {
				airportRouteHandler.getAirportStats (jdbc, routingContext);
			});
			
			//API call - get users statistic 
			router.get("/api/stats/users").handler(routingContext -> {
				userRouteHandler.getTopUsers (jdbc, routingContext);
			});
			
			//API call - get airlines and airplane type statistic 
			router.get("/api/stats/general").handler(routingContext -> {
				airlineRouteHandler.getGeneralStats (jdbc, routingContext);
			});
			
			//API call - get single airport statistic by IATA code
			router.get("/api/stats/airports/detailed/:code").handler(routingContext -> {
				airportRouteHandler.getSingleAirportDetailed (jdbc, routingContext);
			});
			
			//API call - get airlines by partial search
			router.post("/api/airlines/partial/:criteria").handler(routingContext -> {
				airlineRouteHandler.handlePartialSearch (jdbc, routingContext);
			});
			
			//API call - get airlines by partial search
			router.get("/api/airlines/partial/:criteria").handler(routingContext -> {
				airlineRouteHandler.handlePartialSearch (jdbc, routingContext);
			});
			
			//API call - get single airline statistic by IATA code
			router.get("/api/stats/airlines/detailed/:code").handler(routingContext -> {
				airlineRouteHandler.getSingleAirlineDetailedStats (jdbc, routingContext);
			});
			
			//API call - get airline data by IATA code
			router.get("/api/airlines/:code").handler(routingContext -> {
				airlineRouteHandler.getAirline (jdbc, routingContext);
			});
			
			//API call - get airplane type by partial search
			router.post("/api/airplane_types/partial/:criteria").handler(routingContext -> {
				airplaneTypeRouteHandler.handlePartialSearch (jdbc, routingContext);
			});
			
			//API call - get airplane type by partial search
			router.get("/api/airplane_types/partial/:criteria").handler(routingContext -> {
				airplaneTypeRouteHandler.handlePartialSearch (jdbc, routingContext);
			});
			
			//API call - get airplane type data by IATA code
			router.get("/api/airplane_types/:code").handler(routingContext -> {
				airplaneTypeRouteHandler.getAirplaneType (jdbc, routingContext);
			});
			
			//API call - get single airplane type statistic by IATA code
			router.get("/api/stats/airplane_types/detailed/:code").handler(routingContext -> {
				airplaneTypeRouteHandler.getSingleAirplaneTypeDetailedStats (jdbc, routingContext);
			});
			
			
			//API call - get user extended statistics
			router.get("/api/stats/user").handler(routingContext -> {
				flightRouteHandler.handleGetUserStatistics (jdbc, routingContext);
			});
			

			//******************** API ROUTES ************************/

			// Serve the non private static pages
			router.route().handler(StaticHandler.create());

			localHandler.handle(router);

		});
	}

}
