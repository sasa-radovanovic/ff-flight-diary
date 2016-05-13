package frequentFlyer.flight_diary.routeHandlers.errorHandling;

import frequentFlyer.flight_diary.Constants;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;


/**
 * 
 * Factory pattern of Error responses to API calls
 * 
 * @author Sasa Radovanovic
 *
 */
public class ErrorResponseFactory {

	public static void sendError (int statusCode, String message, HttpServerResponse response) {

		switch (statusCode) {
		case 401: {
			if (message != null && message.length() > 0) {
				response.setStatusCode(statusCode).end(new JsonObject().put(Constants.ERROR_REASON, message).encode());
			} else {
				response.setStatusCode(statusCode).end(new JsonObject().put(Constants.ERROR_REASON, Constants.ERROR_401_MSG).encode());
			}
			return;
		}
		case 404: {
			if (message != null && message.length() > 0) {
				response.setStatusCode(statusCode).end(new JsonObject().put(Constants.ERROR_REASON, message).encode());
			} else {
				response.setStatusCode(statusCode).end(new JsonObject().put(Constants.ERROR_REASON, Constants.ERROR_404_MSG).encode());
			}
			return;
		}
		case 409: {
			if (message != null && message.length() > 0) {
				response.setStatusCode(statusCode).end(new JsonObject().put(Constants.ERROR_REASON, message).encode());
			} else {
				response.setStatusCode(statusCode).end(new JsonObject().put(Constants.ERROR_REASON, Constants.ERROR_409_MSG).encode());
			}
			return;
		}
		case 500: {
			if (message != null && message.length() > 0) {
				response.setStatusCode(statusCode).end(new JsonObject().put(Constants.ERROR_REASON, message).encode());
			} else {
				response.setStatusCode(statusCode).end(new JsonObject().put(Constants.ERROR_REASON, Constants.ERROR_500_MSG).encode());
			}
			return;
		}
		default: {
			response.setStatusCode(statusCode).end(new JsonObject().put(Constants.ERROR_REASON, Constants.ERROR_500_MSG).encode());
			return;
		}
		}
	}

}
