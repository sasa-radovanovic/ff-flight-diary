package frequentFlyer.flight_diary.models;

import io.vertx.core.json.JsonObject;

/**
 * @author Sasa Radovanovic
 *
 */
public class User {


	private int id;

	private String mail;
	
	private String username;

	private String password;
	
	private String password_salt;
	
	private boolean activated;

	public User(String mail, String username, String password, String password_salt, boolean activated) {
		this.mail = mail;
		this.username = username;
		this.password = password;
		this.password_salt = password_salt;
		this.activated = activated;
	}
	
	
	public User(JsonObject jsonObject) {
		if (jsonObject.containsKey("id")) {
			this.id = jsonObject.getInteger("id");
		}
		if (jsonObject.containsKey("mail")) {
			this.mail = jsonObject.getString("mail");
		}
		if (jsonObject.containsKey("username")) {
			this.username = jsonObject.getString("username");
		}
	}

	public User(int id, String mail, String username, String password, String password_salt, boolean activated) {
		this.id = id;
		this.mail = mail;
		this.username = username;
		this.password = password;
		this.password_salt = password_salt;
		this.activated = activated;
	}
	
	public User(int id, String username) {
		this.id = id;
		this.username = username;
	}

	public User() {
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword_salt() {
		return password_salt;
	}

	public void setPassword_salt(String password_salt) {
		this.password_salt = password_salt;
	}

	public int getId() {
		return id;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}
	
	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", mail=" + mail + ", username=" + username
				+ ", password=" + password + ", password_salt=" + password_salt
				+ "]";
	}
}