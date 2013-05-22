package com.altamiracorp.reddawn.web.routes.term;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.Term;
import com.altamiracorp.reddawn.ucd.models.TermKey;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.reddawn.web.utils.UrlUtils;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TermByRowKey implements Handler, AppAware {
    private WebApp app;

  public static String getUrl(HttpServletRequest request, TermKey termKey) {
    return UrlUtils.getRootRef(request) + "/terms/" + UrlUtils.urlEncode(termKey.toString());
  }

    @Override
    public void setApp(App app) {
        this.app = (WebApp)app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        UcdClient<AuthorizationLabel> client = app.getUcdClient();
        TermKey termKey = new TermKey(UrlUtils.urlDecode((String) request.getAttribute("rowKey")));
        Term term = client.queryTermByKey(termKey, app.getQueryUser());

        if (term == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setContentType("application/json");
            response.getWriter().write(term.toJson());
        }

        chain.next(request, response);
    }
}
