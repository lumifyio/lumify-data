package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.reddawn.web.utils.UrlUtils;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityMentionsByRange implements Handler, AppAware {
    private TermRepository termRepository = new TermRepository();
    private WebApp app;

    public static String getUrl(HttpServletRequest request, TermRowKey termKey) {
        return UrlUtils.getRootRef(request) + "/term/" + UrlUtils.urlEncode(termKey.toString());
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        String rowKey = UrlUtils.urlDecode((String) request.getAttribute("rowKey"));
        long offset = Long.parseLong(UrlUtils.urlDecode((String) request.getAttribute("offset")));
        long limit = Long.parseLong(UrlUtils.urlDecode((String) request.getAttribute("limit")));

        JSONObject json = new JSONObject();
        JSONArray mentions = new JSONArray();
        for (ColumnFamily mention : termRepository.findMentions(session.getModelSession(), rowKey, offset, limit)) {
            mentions.put(mention.toJson());
        }
        json.put("offset", request.getAttribute("offset"));
        json.put("limit", request.getAttribute("limit"));
        json.put("mentions", mentions);

        new Responder(response).respondWith(json);
        chain.next(request, response);
    }
}
