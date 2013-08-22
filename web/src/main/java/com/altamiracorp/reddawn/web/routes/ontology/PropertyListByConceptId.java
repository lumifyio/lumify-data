package com.altamiracorp.reddawn.web.routes.ontology;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.altamiracorp.reddawn.model.ontology.Property;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class PropertyListByConceptId implements Handler, AppAware {
    private OntologyRepository ontologyRepository = new OntologyRepository();
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);

        List<Property> properties = ontologyRepository.getProperties(session.getGraphSession());

        JSONObject json = new JSONObject();
        json.put("properties", Property.toJson(properties));

        new Responder(response).respondWith(json);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
