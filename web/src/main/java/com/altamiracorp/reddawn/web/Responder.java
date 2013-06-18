package com.altamiracorp.reddawn.web;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Responder {
    private final HttpServletResponse response;

    public Responder(HttpServletResponse response) {
        this.response = response;
    }

    public void respondWith(JSONObject json) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(json.toString());
    }

    public void respondWith(JSONArray json) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(json.toString());
    }

    public void respondWith(String plainText) throws IOException {
        response.setContentType("text/plain");
        response.getWriter().write(plainText);
    }
}
