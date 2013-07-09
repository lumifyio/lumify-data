package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.statementExtraction.SentenceBasedStatementExtractor;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndex;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndexRepository;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
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
        Session session = this.app.getRedDawnSession(request).getModelSession();

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
                String rowKey = new StatementRowKey(
                        fromEntityId,
                        new PredicateRowKey(SentenceBasedStatementExtractor.MODEL_KEY, SentenceBasedStatementExtractor.PREDICATE_LABEL).toString(),
                        toEntityId).toString();
                Statement statement = statementRepository.findByRowKey(session, rowKey);
                if (statement != null) {
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
            if(artifactTermIndex == null) {
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
    }
}
