package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.statement.StatementRepository;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class EntityToEntityRelationship implements Handler, AppAware {
    private StatementRepository statementRepository = new StatementRepository();
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String source = request.getParameter("source");
        String target = request.getParameter("target");
        RedDawnSession session = app.getRedDawnSession(request);

        List<Statement> statements = statementRepository.findBySourceAndTargetRowKey(session.getModelSession(), source, target);

        JSONObject results = resultsToJson(source, target, statements);

        new Responder(response).respondWith(results);
    }

    private JSONObject resultsToJson(String source, String target, List<Statement> statements) throws JSONException {
        JSONObject results = new JSONObject();
        results.put("source", new TermRowKey(source).toJson());
        results.put("target", new TermRowKey(target).toJson());

        JSONArray statementsJson = new JSONArray();
        for (Statement statement : statements) {
            statementsJson.put(statement.toJson());
        }
        results.put("statements", statementsJson);

        return results;
    }
}
