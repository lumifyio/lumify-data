package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndexRepository;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.statement.StatementRepository;
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
import java.util.List;
import java.util.regex.Pattern;

public class EntityRelationshipsBidrectional implements Handler, AppAware {
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
        String rowKey = Pattern.quote(((String) request.getAttribute("rowKey")).replace("$2E$", "."));
        List<Statement> statements = statementRepository.findByRowKeyRegex(session, "(" + rowKey + ".*)|(.*" + rowKey + ")");

        JSONObject json = new JSONObject();
        JSONArray statementsJson = new JSONArray();
        for(Statement statement : statements) {
            statementsJson.put(statement.getRowKey().toJson());
        }

        json.put("statements", statementsJson);
        new Responder(response).respondWith(json);

        chain.next(request, response);
    }
}
