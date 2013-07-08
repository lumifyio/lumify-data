package com.altamiracorp.messaging.users;

import org.json.JSONException;
import org.json.JSONObject;


public class User {

	private String id;
	private String displayName;
	private String status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String toJsonString () throws JSONException{
		return toJson().toString();
	}
	
	public JSONObject toJson () throws JSONException{
		JSONObject json = new JSONObject();
		json.put("id", getId());
		json.put("displayName", getDisplayName());
		json.put("status", getStatus());
		return json;
	}


}
