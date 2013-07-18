package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndex;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndexRepository;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.statement.StatementRepository;
import com.altamiracorp.reddawn.ucd.statement.StatementRowKey;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
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
import java.util.*;

public class EntityRelationships implements Handler, AppAware {
    private WebApp app;
    private StatementRepository statementRepository = new StatementRepository();
    private ArtifactTermIndexRepository artifactTermIndexRepository = new ArtifactTermIndexRepository();

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
 /*       Session session = this.app.getRedDawnSession(request).getModelSession();

        String[] entityIds = request.getParameterValues("entityIds[]");
        if (entityIds == null) {
            entityIds = new String[0];
        }

        String[] artifactIds = request.getParameterValues("artifactIds[]");
        if (artifactIds == null) {
            artifactIds = new String[0];
        }

        // TODO load the relationships from the database
        JSONArray resultsJson = new JSONArray();
        for (String fromEntityId : entityIds) {
            for (String toEntityId : entityIds) {
                List<Statement> statements = statementRepository.findBySourceAndTargetRowKey(session, fromEntityId, toEntityId);
                if (statements.size() > 0) {
                    JSONObject rel = new JSONObject();
                    rel.put("relationshipType", "entityToEntity");
                    rel.put("from", fromEntityId);
                    rel.put("to", toEntityId);
                    resultsJson.put(rel);
                }
            }
        }

        for (String artifactId : artifactIds) {
            ArtifactTermIndex artifactTermIndex = artifactTermIndexRepository.findByRowKey(session, artifactId);
            if (artifactTermIndex == null) {
                continue;
            }
            for (String entityId : entityIds) {
                for (TermRowKey artifactTermMentionTermRowKey : artifactTermIndex.getTermMentions()) {
                    if (artifactTermMentionTermRowKey.toString().equals(entityId)) {
                        JSONObject rel = new JSONObject();
                        rel.put("relationshipType", "artifactToEntity");
                        rel.put("from", artifactId);
                        rel.put("to", entityId);
                        resultsJson.put(rel);
                    }
                }
            }
        }

        new Responder(response).respondWith(resultsJson);
*/
        Session session = this.app.getRedDawnSession(request).getModelSession();

        JSONObject jsonArray = new JSONObject(request.getParameter("json"));
        JSONArray entityIds = jsonArray.getJSONArray("entityIds");
        JSONArray artifactIds = jsonArray.getJSONArray("artifactIds");

        if (entityIds.length() > 0){
            List <String> rowKeyPrefixes = new ArrayList<String>();
            for (int i = 0; i < entityIds.length(); i ++){
                rowKeyPrefixes.add(entityIds.getString(i));
            }

            HashMap<String, HashSet<String>> entityRelationships = statementRepository.findRelationshipDirection(rowKeyPrefixes, session);

            JSONArray resultsJson = new JSONArray();
            for (Map.Entry<String, HashSet<String>> entityRelationship : entityRelationships.entrySet()){
                for (String toEntity : entityRelationship.getValue()){
                    HashSet<String> toEntities = entityRelationships.get(toEntity);
                        JSONObject rel = new JSONObject();
                        if (toEntities.contains(entityRelationship.getKey()) && !toEntity.equals(entityRelationship.getKey())){

                            rel.put ("bidirectional", true);
                            toEntities.remove(entityRelationship.getKey());
                        }
                        rel.put("relationshipType", "entityToEntity");
                        rel.put("from", entityRelationship.getKey());
                        rel.put("to", toEntity);
                        resultsJson.put(rel);
                }
            }
            new Responder(response).respondWith(resultsJson);
            chain.next(request, response);
        }
    }
}
