package frequentFlyer.flight_diary.repos;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import frequentFlyer.flight_diary.Constants;
import frequentFlyer.flight_diary.auth.ApplicationSHAEncoder;
import frequentFlyer.flight_diary.mailer.ApplicationMailer;
import frequentFlyer.flight_diary.mailer.MessageFactory;
import frequentFlyer.flight_diary.models.User;


/**
 * 
 * User Repo
 * 
 * This module does the actual database calls on user table
 * 
 * @author Sasa Radovanovic
 *
 */
public class UsersRepo implements Repo {

	private ApplicationMailer mailer;

	public UsersRepo (ApplicationMailer mailer) {
		this.mailer = mailer;
	}

	@Override
	public void getCount(JDBCClient jdbc, Handler<AsyncResult<Integer>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Could not open SQL connection"));
			}

			conn.query("SELECT count(*) FROM flight_diary_users", query -> {
				if (query.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(query.cause()));
				} else {
					conn.close(hand -> {
						handler.handle(Future.succeededFuture(query.result().getResults().get(0).getInteger(0)));
					});
				}
			});
		});
	}



	@Override
	public void getAllRows (JDBCClient jdbc, Handler<AsyncResult<JsonArray>> handler) {

		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Could not open SQL connection"));
			}

			conn.query("SELECT id, username, mail FROM flight_diary_users", query -> {
				if (query.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(query.cause()));
				} else {
					conn.close(hand -> {
						JsonArray arr = new JsonArray();
						query.result().getRows().forEach(arr::add);
						handler.handle(Future.succeededFuture(arr));
					});
				}
			});
		});
	}

	/**
	 * 
	 * Add a user to the database
	 * 
	 * @param user - User object to add
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void addUser (User user, JDBCClient jdbc, Handler<AsyncResult<User>> handler) {

		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(null);
			}


			final String salt = ApplicationSHAEncoder.getSalt();
			user.setPassword_salt(salt);
			String passwordToEncode = user.getPassword();
			user.setPassword(ApplicationSHAEncoder.computeHash(passwordToEncode, user.getPassword_salt()));


			String sql = "INSERT INTO flight_diary_users (mail, username, password, password_salt, activated) VALUES (?, ?, ?, ?, ?)";
			conn.updateWithParams(sql,
					new JsonArray().add(user.getMail()).add(user.getUsername()).add(user.getPassword()).add(user.getPassword_salt()).add(false),
					(ar) -> {
						if (ar.failed()) {
							handler.handle(Future.failedFuture(ar.cause()));
							conn.close();
							return;
						}
						UpdateResult result = ar.result();

						//Send activation mail, we will use salt for activation token
						mailer.sendMail(user.getMail(), 
								MessageFactory.getMailContent(Constants.REGISTRATION, new JsonObject().put(Constants.REGISTRATION_TOKEN_KEY, salt).put(Constants.DEPLOYMENT_URL_KEY, Constants.DEPLOYMENT_URL)), 
								mailSentHandler -> {

									User createdUser = new User(result.getKeys().getInteger(0), user.getUsername());
									conn.close();
									handler.handle(Future.succeededFuture(createdUser));


								});
					}
					);
		});

	}

	/**
	 * 
	 * Delete a user from the database
	 * 
	 * @param id - User id to delete
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void deleteUser (int id, JDBCClient jdbc, Handler<AsyncResult<Boolean>> handler) {

		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Connection is null"));
			}
			String sql = "DELETE FROM flight_diary_users WHERE id=" + id;
			conn.execute(sql, (ar) -> {
				if (ar.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(ar.cause()));
					return;
				}
				conn.close();
				handler.handle(Future.succeededFuture(true));
			});
		});		
	}

	/**
	 * 
	 * Get user based on his/her username
	 * 
	 * @param username - Username of user
	 * @param jdbc - JDBC connector to create SQL connections
	 * @param handler - Callback on job done
	 */
	public void getUserByUsername (String username, JDBCClient jdbc, Handler<AsyncResult<User>> handler) {

		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Connection is null"));
			}

			conn.queryWithParams("SELECT * FROM flight_diary_users WHERE username=?", new JsonArray().add(username), query -> {
				if (query.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(query.cause()));
				} else {
					conn.close(hand -> {
						User user = new User(query.result().getRows().get(0));
						handler.handle(Future.succeededFuture(user));
					});
				}
			});
		});		
	}



	/**
	 * 
	 * Handle activation of user
	 * 
	 * @param salt - token for activation
	 * @param jdbc - JDBC client to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void activateUser (String salt, JDBCClient jdbc, Handler<AsyncResult<User>> handler) {


		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Connection is null"));
			}
			String sql = "UPDATE flight_diary_users set activated=true WHERE password_salt='" + salt + "'";
			conn.execute(sql, (ar) -> {
				if (ar.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(ar.cause()));
					return;
				}


				conn.query("SELECT id, username, mail FROM flight_diary_users WHERE password_salt='" + salt + "'", userAr -> {
					if (userAr.failed()) {
						conn.close();
						handler.handle(Future.failedFuture(userAr.cause()));
						return;
					}


					User activatedUser = new User(userAr.result().getRows().get(0));

					conn.close();

					handler.handle(Future.succeededFuture(activatedUser));

				});

			});
		});	
	}

	/**
	 * 
	 * Get users with the most flights
	 * 
	 * @param jdbc - JDBC connector to create SQL connection
	 * @param handler - Callback on job done
	 */
	public void getTopUsers(JDBCClient jdbc, Handler<AsyncResult<JsonArray>> handler) {
		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Connection is null"));
			}
			String sql = "SELECT flight_diary_users.username, "
					+ "flight_diary_flights.user_id, count(*) from flight_diary_flights "
					+ "LEFT JOIN flight_diary_users on flight_diary_flights.user_id=flight_diary_users.id "
					+ "GROUP BY flight_diary_flights.user_id, "
					+ "flight_diary_users.username ORDER BY COUNT(*) DESC LIMIT 5";

			conn.query(sql, resultHandler -> {
				if (resultHandler.failed()) {
					conn.close();
					System.out.println(resultHandler.cause());
					handler.handle(Future.failedFuture(resultHandler.cause()));
				} else {
					JsonArray resultArray = new JsonArray();
					resultHandler.result().getRows().forEach(resultArray::add);
					conn.close();
					handler.handle(Future.succeededFuture(resultArray));
				}
			});
		});

	}


	/**
	 * 
	 * Partial search of users based on username criteria
	 * 
	 * @param criteria - Partial String for searching
	 * @param jdbc - JDBC connector for creating SQL connections
	 * @param handler - Callback on job done
	 */
	public void getPartialSearch(String criteria, JDBCClient jdbc, Handler<AsyncResult<JsonArray>> handler) {

		jdbc.getConnection(res -> {
			final SQLConnection conn = res.result();

			if (conn == null) {
				handler.handle(Future.failedFuture("Connection is null"));
			}

			String sql = "SELECT username from flight_diary_users where UPPER(username) like UPPER('%" + criteria + "%') ORDER BY username ASC LIMIT 10";

			conn.query(sql,resultHandler -> {
				if (resultHandler.failed()) {
					conn.close();
					handler.handle(Future.failedFuture(resultHandler.cause()));
				} else {
					conn.close(hand -> {
						JsonArray arr = new JsonArray();
						resultHandler.result().getRows().forEach(arr::add);
						handler.handle(Future.succeededFuture(arr));
					});
				}
			});
		});

	}

}
