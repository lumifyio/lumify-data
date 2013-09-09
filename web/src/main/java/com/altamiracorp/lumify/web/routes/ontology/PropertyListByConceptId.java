package com.altamiracorp.lumify.web.routes.ontology;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.Property;
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

public class PropertyListByConceptId implements Handler, AppAware {
    private OntologyRepository ontologyRepository = new OntologyRepository();
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String conceptId = (String) request.getAttribute("conceptId");
        AppSession session = app.getAppSession(request);

        List<Property> properties = ontologyRepository.getPropertiesByConceptId(session.getGraphSession(), conceptId);

        JSONObject json = new JSONObject();
        json.put("properties", Property.toJsonProperties(properties));

        new Responder(response).respondWith(json);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
