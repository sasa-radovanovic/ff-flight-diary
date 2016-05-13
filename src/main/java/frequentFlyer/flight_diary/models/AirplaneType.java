package frequentFlyer.flight_diary.models;

import io.vertx.core.json.JsonObject;

/**
 * @author Sasa Radovanovic
 *
 */
public class AirplaneType {

	private String type_code;
	
	private String type_name;
	

	public AirplaneType() {
		super();
	}
	
	
	public AirplaneType(JsonObject jsonObject) {
		if (jsonObject.containsKey("type_code")) {
			this.type_code = jsonObject.getString("type_code");
		}
		if (jsonObject.containsKey("type_name")) {
			this.type_name = jsonObject.getString("type_name");
		}
	}

	public AirplaneType(String type_code, String type_name) {
		super();
		this.type_code = type_code;
		this.type_name = type_name;
	}

	public String getType_code() {
		return type_code;
	}

	public void setType_code(String type_code) {
		this.type_code = type_code;
	}

	public String getType_name() {
		return type_name;
	}

	public void setType_name(String type_name) {
		this.type_name = type_name;
	}
	
}
