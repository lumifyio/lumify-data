package com.altamiracorp.reddawn.web.routes.predicate;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.predicate.Predicate;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRepository;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

public class PredicateList implements Handler, AppAware {
    private PredicateRepository predicateRepository = new PredicateRepository();
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);

        Collection<Predicate> predicates = predicateRepository.findAll(session.getModelSession());

        JSONArray resultsJSON = new JSONArray();
        for (Predicate predicate : predicates) {
            JSONObject predicateJson = new JSONObject();
            predicateJson.put("rowKey", predicate.getRowKey().toJson());
            predicateJson.put("labelUi", predicate.getPredicateElements().getLabelUi());
            resultsJSON.put(predicateJson);
        }

        new Responder(response).respondWith(resultsJSON);
        chain.next(request, response);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
