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
import java.util.Map;
import java.util.TreeMap;

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
        String rowKey = ((String) request.getAttribute("rowKey")).replace("$2E$", ".");
        String[] params = UrlUtils.urlDecode(request.getQueryString()).split("&");
        Map<String, String> paramMap = new TreeMap<String, String>();
        for(String param : params) {
            String[] kvPair = param.split("=");
            paramMap.put(kvPair[0], kvPair[1]);
        }
        long offset = Long.parseLong(paramMap.get("offset"));
        long limit = Long.parseLong(paramMap.get("limit"));

        JSONObject json = new JSONObject();
        JSONArray mentions = new JSONArray();
        for (ColumnFamily mention : termRepository.findMentions(session.getModelSession(), rowKey, offset, limit)) {
            mentions.put(mention.toJson());
        }
        json.put("offset", offset);
        json.put("limit", limit);
        json.put("mentions", mentions);

        new Responder(response).respondWith(json);
        chain.next(request, response);
    }
}
