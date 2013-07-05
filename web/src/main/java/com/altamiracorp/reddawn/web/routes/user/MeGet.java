package com.altamiracorp.reddawn.web.routes.user;

import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.User;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MeGet implements Handler {

	@Override
	public void handle(HttpServletRequest request,
			HttpServletResponse response, HandlerChain chain) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject user = new JSONObject();
		user.put("id", User.getUser(request).getId());
		resultJson.put("user", user);
        new Responder(response).respondWith(resultJson);
	}

}
