package com.altamiracorp.lumify.web.routes.admin;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;

public class AdminQuery extends BaseRequestHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String tableName = getRequiredParameter(request, "tableName");
        final String beginKey = getRequiredParameter(request, "beginKey");
        final String endEnd = getRequiredParameter(request, "endEnd");

        AppSession session = app.getAppSession(request);

        List<Row> rows = session.getModelSession().findByRowKeyRange(tableName, beginKey, endEnd, session.getModelSession().getQueryUser());

        JSONObject results = new JSONObject();
        JSONArray rowsJson = new JSONArray();
        for (Row row : rows) {
            rowsJson.put(row.toJson());
        }
        results.put("rows", rowsJson);

        respondWithJson(response, results);
    }
}
