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

        // validate parameters
        String artifactKey = request.getParameter("artifactKey");
        if (artifactKey == null) {
            throw new RuntimeException("'artifactKey' is required.");
        }

        if (request.getParameter("mentionStart") == null) {
            throw new RuntimeException("'mentionStart' is required.");
        }
        long mentionStart = Long.parseLong(request.getParameter("mentionStart"));

        if (request.getParameter("mentionEnd") == null) {
            throw new RuntimeException("'mentionEnd' is required.");
        }
        long mentionEnd = Long.parseLong(request.getParameter("mentionEnd"));

        String sign = request.getParameter("sign");
        if (sign == null) {
            throw new RuntimeException("'sign' is required.");
        }

        String conceptLabel = request.getParameter("conceptLabel");
        if (conceptLabel == null) {
            throw new RuntimeException("'conceptLabel' is required.");
        }

        String newObjectSign = request.getParameter("newObjectSign");
        String newObjectConceptLabel = request.getParameter("newObjectConceptLabel");
        if ((newObjectSign == null && newObjectConceptLabel != null) || (newObjectSign != null && newObjectConceptLabel == null)) {
            throw new RuntimeException("When creating a new object both 'newObjectSign' and 'newObjectConceptLabel' need to be specified.");
        }

        String sentenceRowKey = request.getParameter("sentenceRowKey");
        String objectRowKey = request.getParameter("objectRowKey");
        if (sentenceRowKey != null && (objectRowKey == null || (newObjectSign == null && newObjectConceptLabel == null))) {
            throw new RuntimeException("When associating a Term to and Object 'sentenceRowKey' and ('objectRowKey' or ('newObjectSign' and 'newObjectConceptLabel')) are required.");
        }

        // do the work
        Term term = createTerm(currentUser, session, artifactKey, mentionStart, mentionEnd, sign, conceptLabel);
        resultsJson.put("termRowKey", term.getRowKey().toJson());

        if (newObjectSign != null && newObjectConceptLabel != null) {
            objectRowKey = createObjectTerm(currentUser, session, artifactKey, newObjectSign, newObjectConceptLabel, mentionStart, mentionEnd);
            resultsJson.put("objectTermRowKey", objectRowKey);
        }

        if (sentenceRowKey != null && objectRowKey != null) {
            Statement statement = createIsAnObjectStatement(currentUser, session, artifactKey, sentenceRowKey, objectRowKey, term);
            resultsJson.put("isAnObjectStatementRowKey", statement.getRowKey().toJson());

            UcdObject ucdObject = createObject(session, objectRowKey, statement);
            resultsJson.put("ucdObjectRowKey", ucdObject.getRowKey().toJson());
        }

        new Responder(response).respondWith(resultsJson);
    }

    private Term createTerm(User currentUser, RedDawnSession session, String artifactKey, long mentionStart, long mentionEnd, String sign, String conceptLabel) {
        Term term = new Term(sign, MODEL_KEY, conceptLabel);
        TermMention termMention = new TermMention()
                .setArtifactKey(artifactKey)
                .setMentionStart(mentionStart)
                .setMentionEnd(mentionEnd)
                .setAuthor(currentUser.getUsername())
                .setDate(new Date());
        term.addTermMention(termMention);
        termRepository.save(session.getModelSession(), term);
        return term;
    }

    private UcdObject createObject(RedDawnSession session, String objectRowKey, Statement statement) {
        UcdObject ucdObject = new UcdObject(objectRowKey);
        UcdObjectObjectStatement ucdObjectObjectStatement = new UcdObjectObjectStatement();
        ucdObjectObjectStatement.set(statement.getRowKey().toString(), statement.getRowKey().toString());
        ucdObject.addObjectStatement(ucdObjectObjectStatement);
        ucdObjectRepository.save(session.getModelSession(), ucdObject);
        return ucdObject;
    }

    private Statement createIsAnObjectStatement(User currentUser, RedDawnSession session, String artifactKey, String sentenceRowKey, String objectRowKey, Term term) {
        StatementRowKey statementRowKey = new StatementRowKey(term.getRowKey(), PredicateRowKey.IS_AN_OBJECT, new TermRowKey(objectRowKey));
        Statement statement = new Statement(statementRowKey);
        StatementArtifact statementArtifact = new StatementArtifact()
                .setArtifactKey(artifactKey)
                .setAuthor(currentUser.getUsername())
                .setDate(new Date())
                .setSentence(sentenceRowKey);
        statement.addStatementArtifact(statementArtifact);
        statementRepository.save(session.getModelSession(), statement);
        return statement;
    }

    private String createObjectTerm(User currentUser, RedDawnSession session, String artifactKey, String objectSign, String objectConceptLabel, long mentionStart, long mentionEnd) {
        String objectRowKey;
        Term objectTerm = new Term(objectSign, "object", objectConceptLabel);
        TermMention objectTermMention = new TermMention()
                .setArtifactKey(artifactKey)
                .setMentionStart(mentionStart)
                .setMentionEnd(mentionEnd)
                .setAuthor(currentUser.getUsername())
                .setDate(new Date());
        objectTerm.addTermMention(objectTermMention);
        termRepository.save(session.getModelSession(), objectTerm);
        objectRowKey = objectTerm.getRowKey().toString();
        return objectRowKey;
    }

    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
