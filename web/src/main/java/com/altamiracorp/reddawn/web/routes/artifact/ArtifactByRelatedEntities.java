package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndexRepository;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndex;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.web.*;
import com.altamiracorp.web.App;
import com.altamiracorp.web.utils.UrlUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

public class ArtifactByRelatedEntities implements Handler, AppAware {
    private WebApp app;
    private ArtifactTermIndexRepository artifactTermIndexRepository = new ArtifactTermIndexRepository();

    @Override
    public void setApp (App app){
        this.app = (WebApp) app;
    }

    @Override
    public void handle (HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        Session session = this.app.getRedDawnSession(request).getModelSession();
        String rowKey = UrlUtils.urlDecode((String) request.getAttribute("rowKey"));
        ArtifactTermIndex artifactTermIndexes = artifactTermIndexRepository.findByRowKey(session, rowKey);

        Collection <ColumnFamily> columnFamilies = artifactTermIndexes.getColumnFamilies();

        JSONArray terms = new JSONArray();
        for (ColumnFamily columnFamily : columnFamilies){
            JSONObject json = new JSONObject();
            TermRowKey termRowKey = new TermRowKey(columnFamily.getColumnFamilyName());
            json.put("rowKey", columnFamily.getColumnFamilyName());
            json.put("subType", termRowKey.getConceptLabel());
            json.put("title", termRowKey.getSign());
            json.put("type", "entity");
            terms.put(json);
        }

        new Responder (response).respondWith(terms);
        chain.next(request, response);
    }
}
