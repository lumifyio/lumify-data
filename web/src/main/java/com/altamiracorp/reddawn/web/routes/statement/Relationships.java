package com.altamiracorp.reddawn.web.routes.statement;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.altamiracorp.reddawn.model.ontology.Relationship;
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

public class Relationships implements Handler, AppAware {
    private OntologyRepository ontologyRepository = new OntologyRepository();
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        String sourceConceptTypeId = getOptionalParameter(request, "sourceConceptTypeId");
        String destConceptTypeId = getOptionalParameter(request, "destConceptTypeId");

        List<Relationship> relationships = ontologyRepository.getRelationships(session.getGraphSession(), sourceConceptTypeId, destConceptTypeId);

        JSONObject result = new JSONObject();
        JSONArray relationshipsJson = new JSONArray();
        for (Relationship relationship : relationships) {
            relationshipsJson.put(toJson(relationship));
        }
        result.put("relationships", relationshipsJson);

        new Responder(response).respondWith(result);
        chain.next(request, response);
    }

    private String getOptionalParameter(HttpServletRequest request, String name) {
        String val = request.getParameter(name);
        if (val == null) {
            return null;
        }
        return UrlUtils.urlDecode(val);
    }

    private JSONObject toJson(Relationship relationship) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", relationship.getId());
        json.put("title", relationship.getTitle());
        return json;
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
