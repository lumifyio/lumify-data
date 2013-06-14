package com.altamiracorp.messaging.users;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Users {

	private List<User> users;

	public List<User> getUsers() {
		if (users == null) {
			users = new ArrayList<User>();
		}
		
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
	
	public String toJson () throws JSONException {
		JSONObject json = new JSONObject ();
		JSONArray users = new JSONArray();
		for (User user : getUsers()) {
			users.put(user.toJson());
		}
		
		json.put("users", users);
		return json.toString();
	}

}
