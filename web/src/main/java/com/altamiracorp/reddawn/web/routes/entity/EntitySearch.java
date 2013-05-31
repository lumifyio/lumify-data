package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.model.Term;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

// TODO: change this over to an Entity search once entities work
public class EntitySearch implements Handler, AppAware {
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String query = request.getParameter("q");

        UcdClient<AuthorizationLabel> client = app.getUcdClient();

        List<Term> terms = client.queryTermStartsWith(query, app.getQueryUser());

        JSONObject termsJson = termsToSearchResults(terms, request);

        response.setContentType("application/json");
        response.getWriter().write(termsJson.toString());

    }

    private JSONObject termsToSearchResults(List<Term> terms, HttpServletRequest request) throws JSONException {
        JSONObject termsJson = new JSONObject();
        for (Term term : terms) {
            JSONArray conceptJson = null;
            if (termsJson.has(term.getKey().getConcept())) {
                conceptJson = (JSONArray) termsJson.get(term.getKey().getConcept());
            }
            if (conceptJson == null) {
                conceptJson = new JSONArray();
                termsJson.put(term.getKey().getConcept(), conceptJson);
            }
            JSONObject termJson = new JSONObject();
            termJson.put("url", EntityByRowKey.getUrl(request, term.getKey()));
            termJson.put("rowKey", term.getKey().toString());
            termJson.put("sign", term.getKey().getSign());
            termJson.put("model", term.getKey().getModel());
            conceptJson.put(termJson);
        }
        return termsJson;
    }

}
