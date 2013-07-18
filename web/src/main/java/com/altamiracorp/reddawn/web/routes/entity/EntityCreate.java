package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.object.UcdObject;
import com.altamiracorp.reddawn.ucd.object.UcdObjectObjectStatement;
import com.altamiracorp.reddawn.ucd.object.UcdObjectRepository;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.statement.StatementArtifact;
import com.altamiracorp.reddawn.ucd.statement.StatementRepository;
import com.altamiracorp.reddawn.ucd.statement.StatementRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityCreate.class.getName());
    private static final String MODEL_KEY = "manual";
    private WebApp app;
    private TermRepository termRepository = new TermRepository();
    private StatementRepository statementRepository = new StatementRepository();
    private UcdObjectRepository ucdObjectRepository = new UcdObjectRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User currentUser = User.getUser(request);
        RedDawnSession session = app.getRedDawnSession(request);
        JSONObject resultsJson = new JSONObject();

        String artifactKey = request.getParameter("artifactKey");
        long mentionStart = Long.parseLong(request.getParameter("mentionStart"));
        long mentionEnd = Long.parseLong(request.getParameter("mentionEnd"));
        String sign = request.getParameter("sign");
        String conceptLabel = request.getParameter("conceptLabel");
        String sentenceRowKey = request.getParameter("sentenceRowKey");
        String objectRowKey = request.getParameter("objectRowKey");
        String newObjectSign = request.getParameter("newObjectSign");
        String newObjectConceptLabel = request.getParameter("newObjectConceptLabel");

        Term term = new Term(sign, MODEL_KEY, conceptLabel);
        TermMention termMention = new TermMention()
                .setArtifactKey(artifactKey)
                .setMentionStart(mentionStart)
                .setMentionEnd(mentionEnd)
                .setAuthor(currentUser.getUsername())
                .setDate(new Date());
        term.addTermMention(termMention);
        termRepository.save(session.getModelSession(), term);
        resultsJson.put("termRowKey", term.getRowKey().toJson());

        if (newObjectSign != null && newObjectConceptLabel != null) {
            Term objectTerm = new Term(newObjectSign, "object", newObjectConceptLabel);
            TermMention objectTermMention = new TermMention()
                    .setArtifactKey(artifactKey)
                    .setMentionStart(mentionStart)
                    .setMentionEnd(mentionEnd)
                    .setAuthor(currentUser.getUsername())
                    .setDate(new Date());
            objectTerm.addTermMention(objectTermMention);
            termRepository.save(session.getModelSession(), objectTerm);
            resultsJson.put("objectTermRowKey", objectTerm.getRowKey().toJson());

            objectRowKey = objectTerm.getRowKey().toString();
        }

        if (sentenceRowKey != null && objectRowKey != null) {
            StatementRowKey statementRowKey = new StatementRowKey(term.getRowKey(), PredicateRowKey.IS_AN_OBJECT, new TermRowKey(objectRowKey));
            Statement statement = new Statement(statementRowKey);
            StatementArtifact statementArtifact = new StatementArtifact()
                    .setArtifactKey(artifactKey)
                    .setAuthor(currentUser.getUsername())
                    .setDate(new Date())
                    .setSentence(sentenceRowKey);
            statement.addStatementArtifact(statementArtifact);
            statementRepository.save(session.getModelSession(), statement);
            resultsJson.put("isAnObjectStatementRowKey", statement.getRowKey().toJson());

            UcdObject ucdObject = new UcdObject(objectRowKey);
            UcdObjectObjectStatement ucdObjectObjectStatement = new UcdObjectObjectStatement();
            ucdObjectObjectStatement.set(statement.getRowKey().toString(), statement.getRowKey().toString());
            ucdObject.addObjectStatement(ucdObjectObjectStatement);
            ucdObjectRepository.save(session.getModelSession(), ucdObject);
            resultsJson.put("ucdObjectRowKey", ucdObject.getRowKey().toJson());
        }

        new Responder(response).respondWith(resultsJson);
    }

    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
