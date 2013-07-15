package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.statement.StatementRepository;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.reddawn.web.utils.UrlUtils;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.web.*;
import com.altamiracorp.web.App;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class EntityByRelatedEntities implements Handler, AppAware{
    private WebApp app;
    private StatementRepository statementRepository = new StatementRepository();

    @Override
    public void setApp (App app){
        this.app = (WebApp) app;
    }

    @Override
    public void handle (HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        Session session = this.app.getRedDawnSession(request).getModelSession();
        String rowKey = UrlUtils.urlDecode ((String) request.getAttribute("rowKey"));
        List<Statement> statements = statementRepository.findByRowStartsWith(session, rowKey);

        JSONObject json = new JSONObject();
        JSONArray statementsJson = new JSONArray();
        for (Statement statement : statements){
            statementsJson.put(new TermRowKey(statement.getRowKey().getObjectRowKey()).toJson());
        }
        json.put("statements", statementsJson);

        new Responder(response).respondWith(json);
        chain.next(request, response);
    }
}
