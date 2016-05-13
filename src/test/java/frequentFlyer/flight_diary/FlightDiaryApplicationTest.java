package frequentFlyer.flight_diary;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This is our JUnit test for our verticle. The test uses vertx-unit, so we declare a custom runner.
 * NOTE: Application must be startet with cleand database filled with fixtures
 * NOTE 2: I know this can be done much better. Once i find time - i will rewrite the whole start-up procedure
 */
@RunWith(VertxUnitRunner.class)
public class FlightDiaryApplicationTest {

	private Vertx vertx;
	private Integer port;

	/**
	 * Before executing our test, let's deploy our verticle.
	 * This method instantiates a new Vertx and deploy the verticle. Then, it waits in the verticle has successfully
	 * completed its start sequence (thanks to `context.asyncAssertSuccess`).
	 *
	 * @param context the test context.
	 */
	@Before
	public void setUp(TestContext context) throws IOException {
		vertx = Vertx.vertx();

		ServerSocket socket = new ServerSocket(0);
		port = 8080;
		socket.close();

		DeploymentOptions options = new DeploymentOptions()
		.setConfig(new JsonObject().put("http.port", port)
				);

		// We pass the options as the second parameter of the deployVerticle method.
		vertx.deployVerticle(FlightDiaryApplication.class.getName(), options, res -> {
			if (res.succeeded()) {
				context.assertTrue(true);
			}
		});

		try {
			System.out.println(this.getClass().getSimpleName() + " | Wait for test application to be deployed");
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tear down on the end of every test
	 */
	@After
	public void tearDown(TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | Test Vertx Application - TEAR DOWN ");
		//vertx.close(context.asyncAssertSuccess());
	}

	/**
	 * Simple assert to TTT (test-the-test)
	 */
	@Test
	public void checkTestingFramework(TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 1 | checkTestingFramework");
		Assert.assertEquals(10, 10);
	}


	/**
	 * Check that Verticle is deployed and web page is available
	 */
	@Test
	public void checkThatTheIndexPageIsServed(TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 2 | checkThatTheIndexPageIsServed on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/", response -> {
			context.assertEquals(response.statusCode(), 200);
			context.assertEquals(response.headers().get("content-type"), "text/html");
			response.bodyHandler(body -> {
				context.assertTrue(body.toString().contains("ng-view"));
				async.complete();
			});
		});
	}

	/**
	 * Check that Application returns airport stats
	 */
	@Test
	public void checkAirportStats (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 3 | checkAirportStats on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/stats/airports", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonObject jsonResponse = new JsonObject(body.toString());
				JsonObject byRegionObject = jsonResponse.getJsonObject("byRegionMap");
				context.assertEquals(153, jsonResponse.getInteger("totalUsedAirports"));
				context.assertEquals(122, jsonResponse.getInteger("northernAirports"));
				context.assertEquals(31, jsonResponse.getInteger("southernAirports"));
				context.assertEquals(31, byRegionObject.getInteger("Asia"));
				context.assertEquals(6, byRegionObject.getInteger("Pacific"));
				context.assertEquals(54, byRegionObject.getInteger("Europe"));
				context.assertEquals(40, byRegionObject.getInteger("America"));
				context.assertEquals(10, byRegionObject.getInteger("Africa"));
				async.complete();
			});
		});
	}

	/**
	 * Check that Application returns general stats
	 */
	@Test
	public void checkGeneralStats (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 4 | checkGeneralStats on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/stats/general", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonObject jsonResponse = new JsonObject(body.toString());
				JsonArray airlinesArray = jsonResponse.getJsonArray("airlines");
				context.assertEquals(5, airlinesArray.size());
				JsonObject deltaJsonObject = airlinesArray.getJsonObject(0);
				context.assertEquals("DL", deltaJsonObject.getString("airline_code"));
				context.assertEquals(15, deltaJsonObject.getInteger("count"));
				JsonArray airplanesArray = jsonResponse.getJsonArray("airplaneTypes");
				context.assertEquals(5, airplanesArray.size());
				JsonObject a320JsonObject = airplanesArray.getJsonObject(0);
				context.assertEquals("320", a320JsonObject.getString("airplane_code"));
				context.assertEquals(33, a320JsonObject.getInteger("count"));
				async.complete();
			});
		});
	}


	/**
	 * Check that Application returns airports stats
	 */
	@Test
	public void checkSingleAirportStats (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 5 | checkSingleAirportStats on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/stats/airports/detailed/BEG", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonObject jsonResponse = new JsonObject(body.toString());
				context.assertEquals(16, jsonResponse.getInteger("visited"));
				JsonArray connections = jsonResponse.getJsonArray("connectionAirports");
				context.assertEquals(16, connections.size());
				JsonObject hamObject = connections.getJsonObject(0);
				context.assertEquals("HAM", hamObject.getString("iata_code"));
				JsonObject userObject = jsonResponse.getJsonObject("usersOnAirport");
				context.assertEquals(9, userObject.getInteger("rsasa"));
				context.assertEquals(7, userObject.getInteger("test"));
				async.complete();
			});
		});
	}

	/**
	 * Check that Application returns stats for single airline
	 */
	@Test
	public void checkSingleAirlineStats (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 6 | checkSingleAirlineStats on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/stats/airlines/detailed/JU", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonObject jsonResponse = new JsonObject(body.toString());
				context.assertEquals(7, jsonResponse.getJsonObject("frequentUsers").getInteger("test"));
				context.assertEquals(7, jsonResponse.getInteger("totalFlights"));
				context.assertEquals(3.0, jsonResponse.getDouble("averageRating"));
				JsonObject flightList = jsonResponse.getJsonObject("flightsList");
				JsonObject budBeg = flightList.getJsonObject("BUD-BEG");
				context.assertEquals(47.436933, budBeg.getDouble("longitude_one"));
				context.assertEquals(44.818444, budBeg.getDouble("longitude_two"));
				async.complete();
			});
		});
	}


	/**
	 * Check that Application returns stats for single airline
	 */
	@Test
	public void checkGetAirline (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 7 | checkGetAirline on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/airlines/W6", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonObject jsonResponse = new JsonObject(body.toString());
				context.assertEquals("Wizz Air", jsonResponse.getString("airline_name"));
				context.assertEquals("W6", jsonResponse.getString("airline_iata_code"));
				context.assertEquals("WZZ", jsonResponse.getString("airline_icao_code"));
				context.assertEquals("Hungary", jsonResponse.getString("airline_country"));
				context.assertEquals("WIZZ AIR", jsonResponse.getString("airline_callsign"));
				async.complete();
			});
		});
	}

	/**
	 * Check that Application returns object for airport
	 */
	@Test
	public void checkGetAirport (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 8 | checkGetAirport on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/airports/KVO", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonObject jsonResponse = new JsonObject(body.toString());
				context.assertEquals("Morava Airport", jsonResponse.getString("airport_name"));
				context.assertEquals("Kraljevo/Ladjevci", jsonResponse.getString("airport_location"));
				context.assertEquals("KVO", jsonResponse.getString("iata_code"));
				context.assertEquals("LYKV", jsonResponse.getString("icao_code"));
				context.assertEquals(43.818171, jsonResponse.getDouble("longitude"));
				context.assertEquals(20.585971, jsonResponse.getDouble("latitude"));
				context.assertEquals("Europe/Belgrade", jsonResponse.getString("area_timezone"));
				async.complete();
			});
		});
	}


	/**
	 * Check that Application returns object for airplane type stats
	 */
	@Test
	public void checkGetAirplaneType (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 9 | checkGetAirplaneType on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/airplane_types/AT7", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonObject jsonResponse = new JsonObject(body.toString());
				context.assertEquals("AT7", jsonResponse.getString("type_code"));
				context.assertEquals("Aerospatiale/Alenia ATR 72", jsonResponse.getString("type_name"));
				async.complete();
			});
		});
	}


	/**
	 * Check that Application returns object for single airplane type stats
	 */
	@Test
	public void checkSingleAirplaneTypeStats (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 10 | checkSingleAirplaneTypeStats on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/stats/airplane_types/detailed/388", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonObject jsonResponse = new JsonObject(body.toString());
				context.assertEquals(7, jsonResponse.getInteger("totalFlights"));
				context.assertEquals(5.0, jsonResponse.getDouble("averageRating"));
				context.assertEquals(3, jsonResponse.getJsonArray("usersFlown").size());
				context.assertEquals(2, jsonResponse.getJsonObject("airports").getInteger("DXB"));
				async.complete();
			});
		});
	}

	/**
	 * Check that Application returns object for user stats
	 */
	@Test
	public void checkUserStats (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 11 | checkUserStats on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/stats/users", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonArray jsonResponse = new JsonArray(body.toString());
				context.assertEquals(5, jsonResponse.size());
				context.assertEquals("rsasa", jsonResponse.getJsonObject(0).getString("username"));
				async.complete();
			});
		});
	}


	/**
	 * Check that Application returns object for users flights
	 */
	@Test
	public void checkUserFlights (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 12 | checkUserFlights on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/flights/rsasa", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonArray jsonResponse = new JsonArray(body.toString());
				context.assertEquals(61, jsonResponse.size());
				async.complete();
			});
		});
	}


	/**
	 * Check that Application returns number of users
	 */
	@Test
	public void checkUsersCount (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 13 | checkUsersCount on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/users/count", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				context.assertEquals(10, Integer.parseInt(body.toString()));
				async.complete();
			});
		});
	}




	/**
	 * Check that Application supports partial search of airports
	 */
	@Test
	public void checkPartialSearchAirports (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 14 | checkPartialSearchAirports on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/airports/partial/belgra", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonArray jsonResponse = new JsonArray(body.toString()); 
				context.assertEquals(1, jsonResponse.size());
				context.assertEquals("Beograd Nikola Tesla", jsonResponse.getJsonObject(0).getString("airport_name"));
				async.complete();
			});
		});
	}

	
	/**
	 * Check that Application supports partial search of airlines
	 */
	@Test
	public void checkPartialSearchAirlines (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 15 | checkPartialSearchAirlines on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/airlines/partial/serbia", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonArray jsonResponse = new JsonArray(body.toString()); 
				context.assertEquals(2, jsonResponse.size());
				context.assertEquals("Air Serbia", jsonResponse.getJsonObject(0).getString("airline_name"));
				async.complete();
			});
		});
	}


	/**
	 * Check that Application supports partial search of airplane types
	 */
	@Test
	public void checkPartialSearchAirplanes (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 16 | checkPartialSearchAirplanes on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/airplane_types/partial/380-80", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonArray jsonResponse = new JsonArray(body.toString()); 
				context.assertEquals(1, jsonResponse.size());
				context.assertEquals("388", jsonResponse.getJsonObject(0).getString("type_code"));
				async.complete();
			});
		});
	}


	/**
	 * Check that Application supports partial search of users
	 */
	@Test
	public void checkPartialSearchUsers (TestContext context) {
		System.out.println(this.getClass().getSimpleName() + " | test 17 | checkPartialSearchUsers on port " + port);
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/users/partial/rsasa", response -> {
			context.assertEquals(response.statusCode(), 200);
			response.bodyHandler(body -> {
				JsonArray jsonResponse = new JsonArray(body.toString()); 
				context.assertEquals(1, jsonResponse.size());
				context.assertEquals("rsasa", jsonResponse.getJsonObject(0).getString("username"));
				async.complete();
			});
		});
	}

}