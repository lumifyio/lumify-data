package com.altamiracorp.reddawn.web.routes.admin;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
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

public class AdminQuery implements Handler, AppAware {
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String tableName = request.getParameter("tableName");
        String beginKey = request.getParameter("beginKey");
        String endEnd = request.getParameter("endEnd");
        RedDawnSession session = app.getRedDawnSession(request);

        List<Row> rows = session.getModelSession().findByRowKeyRange(tableName, beginKey, endEnd, session.getModelSession().getQueryUser());

        JSONObject results = new JSONObject();
        JSONArray rowsJson = new JSONArray();
        for (Row row : rows) {
            rowsJson.put(row.toJson());
        }
        results.put("rows", rowsJson);

        new Responder(response).respondWith(results);
    }
}
