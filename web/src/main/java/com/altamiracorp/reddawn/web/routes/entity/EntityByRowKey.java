package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.reddawn.web.utils.UrlUtils;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: change this over to an Entity once entities work
public class EntityByRowKey implements Handler, AppAware {
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
        TermRowKey termKey = new TermRowKey(((String) request.getAttribute("rowKey")).replace("$2E$", "."));

        JSONObject json = new JSONObject();
        json.put("key", termKey.toJson());
        new Responder(response).respondWith(json);

        chain.next(request, response);
    }
}
