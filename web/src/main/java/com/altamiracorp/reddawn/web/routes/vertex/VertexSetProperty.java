package com.altamiracorp.reddawn.web.routes.vertex;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.altamiracorp.reddawn.model.ontology.Property;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class VertexSetProperty implements Handler, AppAware {
    private WebApp app;
    private GraphRepository graphRepository = new GraphRepository();
    private OntologyRepository ontologyRepository = new OntologyRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        String graphVertexId = (String) request.getAttribute("graphVertexId");
        String propertyName = request.getParameter("propertyName");
        String valueStr = request.getParameter("value");

        Property property = ontologyRepository.getProperty(session.getGraphSession(), propertyName);
        Object value = property.convertString(valueStr);

        graphRepository.setProperty(session.getGraphSession(), graphVertexId, propertyName, value);

        Map<String, String> properties = graphRepository.getProperties(session.getGraphSession(), graphVertexId);
        JSONArray resultsJson = VertexProperties.propertiesToJson(properties);
        new Responder(response).respondWith(resultsJson);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
