package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
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
    private TermRepository termRepository = new TermRepository();
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String query = request.getParameter("q");

        RedDawnSession session = app.getRedDawnSession(request);

        List<Term> terms = termRepository.findByRowStartsWith(session.getModelSession(), query.toLowerCase());

        JSONObject termsJson = termsToSearchResults(terms, request);

        response.setContentType("application/json");
        response.getWriter().write(termsJson.toString());

    }

    private JSONObject termsToSearchResults(List<Term> terms, HttpServletRequest request) throws JSONException {
        JSONObject termsJson = new JSONObject();
        for (Term term : terms) {
            JSONArray conceptJson = null;
            if (termsJson.has(term.getRowKey().getConceptLabel())) {
                conceptJson = (JSONArray) termsJson.get(term.getRowKey().getConceptLabel());
            }
            if (conceptJson == null) {
                conceptJson = new JSONArray();
                termsJson.put(term.getRowKey().getConceptLabel(), conceptJson);
            }
            JSONObject termJson = new JSONObject();
            termJson.put("url", EntityByRowKey.getUrl(request, term.getRowKey()));
            termJson.put("rowKey", term.getRowKey().toString());
            termJson.put("sign", term.getRowKey().getSign());
            termJson.put("model", term.getRowKey().getModelKey());
            conceptJson.put(termJson);
        }
        return termsJson;
    }

}
