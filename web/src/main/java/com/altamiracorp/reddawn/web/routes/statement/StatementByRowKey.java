package com.altamiracorp.reddawn.web.routes.statement;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.statement.StatementRepository;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StatementByRowKey implements Handler, AppAware {
    private StatementRepository statementRepository = new StatementRepository();
    private WebApp app;

    public static String getUrl(HttpServletRequest request, String statementRowKey) {
        return UrlUtils.getRootRef(request) + "/statement/" + UrlUtils.urlEncode(statementRowKey.toString());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        ArtifactRowKey statementRowKey = new ArtifactRowKey(UrlUtils.urlDecode((String) request.getAttribute("rowKey")));
        Statement statement = statementRepository.findByRowKey(session.getModelSession(), statementRowKey.toString());

        if (statement == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            JSONObject statementJson = statement.toJson();
            new Responder(response).respondWith(statementJson);
        }

        chain.next(request, response);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
