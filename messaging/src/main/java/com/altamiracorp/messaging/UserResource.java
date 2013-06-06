package com.altamiracorp.messaging;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.altamiracorp.messaging.users.UserUtil;
import com.altamiracorp.messaging.users.Users;

@Path("/user")
public class UserResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Users getLoggedInUsers() {
		return UserUtil.getLoggedInUsers();
	}
}
