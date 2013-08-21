package com.altamiracorp.reddawn.web.routes.ontology;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.ontology.Concept;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
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
        RedDawnSession session = app.getRedDawnSession(request);

        Concept entityConcept = ontologyRepository.getEntityConcept(session.getGraphSession());

        JSONObject result = buildJsonTree(request, session, entityConcept);

        new Responder(response).respondWith(result);
        chain.next(request, response);
    }

    private JSONObject buildJsonTree(HttpServletRequest request, RedDawnSession session, Concept concept) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("id", concept.getId());
        result.put("title", concept.getTitle());
        if (concept.getGlyphIcon() != null) {
            result.put("glyphIconResourceRowKey", concept.getGlyphIcon());
            result.put("glyphIconHref", concept.getGlyphIcon());
        }
        if (concept.getColor() != null) {
            result.put("color", concept.getColor());
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
