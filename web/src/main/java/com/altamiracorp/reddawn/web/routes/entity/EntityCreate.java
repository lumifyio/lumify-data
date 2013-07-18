package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.User;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

public class EntityCreate implements Handler, AppAware {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(EntityCreate.class.getName());
    private static final String MODEL_KEY = "manual";
    private WebApp app;
    private TermRepository termRepository = new TermRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User currentUser = User.getUser(request);
        RedDawnSession session = app.getRedDawnSession(request);

        String artifactKey = request.getParameter("artifactKey");
        long mentionStart = Long.parseLong(request.getParameter("mentionStart"));
        long mentionEnd = Long.parseLong(request.getParameter("mentionEnd"));
        String sign = request.getParameter("sign");
        String conceptLabel = request.getParameter("conceptLabel");
        String sentenceRowKey = request.getParameter("sentenceRowKey"); // TODO don't need this until we start creating objects/entities
        String objectRowKey = request.getParameter("objectRowKey"); // TODO don't need this until we start creating objects/entities

        Term term = new Term(sign, MODEL_KEY, conceptLabel);
        TermMention termMention = new TermMention()
                .setArtifactKey(artifactKey)
                .setMentionStart(mentionStart)
                .setMentionEnd(mentionEnd)
                .setAuthor(currentUser.getUsername())
                .setDate(new Date());
        term.addTermMention(termMention);
        termRepository.save(session.getModelSession(), term);

        JSONObject resultsJson = new JSONObject();
        resultsJson.put("termRowKey", term.getRowKey().toJson());

        new Responder(response).respondWith(resultsJson);
    }

    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
