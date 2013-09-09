package com.altamiracorp.lumify.web.routes.ontology;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.ontology.Concept;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.atteo.evo.inflector.English;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class ConceptList implements Handler, AppAware {
    private OntologyRepository ontologyRepository = new OntologyRepository();
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);

        Concept rootConcept = ontologyRepository.getRootConcept(session.getGraphSession());

        JSONObject result = buildJsonTree(request, session, rootConcept);

        new Responder(response).respondWith(result);
        chain.next(request, response);
    }

    private JSONObject buildJsonTree(HttpServletRequest request, AppSession session, Concept concept) throws JSONException {
        JSONObject result = concept.toJson();

        String displayName = result.optString(PropertyName.DISPLAY_NAME.toString());
        if (displayName != null) {
            result.put("pluralDisplayName", English.plural(displayName));
        }

        List<Concept> childConcepts = ontologyRepository.getChildConcepts(session.getGraphSession(), concept);
        if (childConcepts.size() > 0) {
            JSONArray childrenJson = new JSONArray();
            for (Concept childConcept : childConcepts) {
                JSONObject childJson = buildJsonTree(request, session, childConcept);
                childrenJson.put(childJson);
            }
            result.put("children", childrenJson);
        }

        return result;
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
