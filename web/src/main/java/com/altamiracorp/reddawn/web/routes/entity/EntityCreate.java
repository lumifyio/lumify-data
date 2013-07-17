package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.ucd.term.TermRepository;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.User;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityCreate implements Handler {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(EntityCreate.class.getName());
    private WebApp app;
    private TermRepository termRepository = new TermRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User currentUser = User.getUser(request);

        String termText = request.getParameter("termText");
        String conceptType = request.getParameter("conceptType");

        Thread.sleep(1000);

        // TODO: create term
        //RedDawnSession session = app.getRedDawnSession(request);
        //termRepository.save(session.getModelSession(), createTerm(currentUser, null, null, null, null, null));

        JSONObject termsJson = new JSONObject();
        // TODO: what to put here

        new Responder(response).respondWith(termsJson);
    }

    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
