package com.altamiracorp.lumify.web.routes.ontology;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.Relationship;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class RelationshipLabelList implements Handler, AppAware {
    private OntologyRepository ontologyRepository = new OntologyRepository();
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);

        List<Relationship> relationships = ontologyRepository.getRelationshipLabels(session.getGraphSession());

        JSONObject json = new JSONObject();
        json.put("relationships", Relationship.toJsonRelationships(relationships));

        new Responder(response).respondWith(json);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
