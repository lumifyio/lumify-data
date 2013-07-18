package com.altamiracorp.reddawn.web.routes.concept;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.concept.Concept;
import com.altamiracorp.reddawn.ucd.concept.ConceptRepository;
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

public class ConceptList implements Handler, AppAware {
    private ConceptRepository conceptRepository = new ConceptRepository();
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);

        Collection<Concept> concepts = conceptRepository.findAll(session.getModelSession());

        JSONArray resultsJSON = new JSONArray();
        for (Concept concept : concepts) {
            JSONObject conceptJson = new JSONObject();
            conceptJson.put("conceptLabel", concept.getRowKey().getConceptLabel());
            conceptJson.put("ui", concept.getConceptElements().getLabelUi());
            resultsJSON.put(conceptJson);
        }

        new Responder(response).respondWith(resultsJSON);
        chain.next(request, response);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
