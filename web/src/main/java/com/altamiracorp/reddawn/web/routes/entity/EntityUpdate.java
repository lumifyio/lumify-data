package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityUpdate implements Handler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String termRowKey = request.getParameter("rowKey");

        Thread.sleep(1000);

        JSONObject termsJson = new JSONObject();
        // TODO: ??

        new Responder(response).respondWith(termsJson);
    }
}
