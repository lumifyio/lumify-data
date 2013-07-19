package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.entityHighlight.TermAndTermMentionOffsetItem;
import com.altamiracorp.reddawn.ucd.object.UcdObject;
import com.altamiracorp.reddawn.ucd.object.UcdObjectObjectStatement;
import com.altamiracorp.reddawn.ucd.object.UcdObjectRepository;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.statement.StatementArtifact;
import com.altamiracorp.reddawn.ucd.statement.StatementRepository;
import com.altamiracorp.reddawn.ucd.statement.StatementRowKey;
import com.altamiracorp.reddawn.ucd.term.*;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.User;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

public class EntityCreate implements Handler, AppAware {
    private static final String MODEL_KEY = "manual";
    private WebApp app;
    private TermRepository termRepository = new TermRepository();
    private StatementRepository statementRepository = new StatementRepository();
    private UcdObjectRepository ucdObjectRepository = new UcdObjectRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User currentUser = User.getUser(request);
        RedDawnSession session = app.getRedDawnSession(request);

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
        String sentenceRowKey = request.getParameter("sentenceRowKey");
        String objectRowKey = request.getParameter("objectRowKey");
        if (sentenceRowKey == null && (objectRowKey != null || newObjectSign != null)) {
            throw new RuntimeException("When associating a Term to and Object 'sentenceRowKey' and ('objectRowKey' or 'newObjectSign')) are required.");
        }

        // do the work
        TermAndTermMention termAndTermMention = createTerm(currentUser, artifactKey, mentionStart, mentionEnd, sign, conceptLabel);

        if (newObjectSign != null) {
            TermAndTermMention objectTermAndTermMention = createObjectTerm(currentUser, artifactKey, newObjectSign, conceptLabel, mentionStart, mentionEnd);
            objectRowKey = objectTermAndTermMention.getTerm().getRowKey().toString();

            termRepository.save(session.getModelSession(), objectTermAndTermMention.getTerm());
        }

        if (sentenceRowKey != null && objectRowKey != null) {
            Statement isAnObjectStatement = createIsAnObjectStatement(currentUser, artifactKey, sentenceRowKey, objectRowKey, termAndTermMention.getTerm());

            UcdObject ucdObject = createObject(objectRowKey, isAnObjectStatement);

            termAndTermMention.getTermMention().setObjectRowKey(ucdObject.getRowKey().toString());

            statementRepository.save(session.getModelSession(), isAnObjectStatement);
            ucdObjectRepository.save(session.getModelSession(), ucdObject);
        }

        termRepository.save(session.getModelSession(), termAndTermMention.getTerm());

        TermAndTermMentionOffsetItem offsetItem = new TermAndTermMentionOffsetItem(termAndTermMention);
        new Responder(response).respondWith(offsetItem.toJson());
    }

    private static TermAndTermMention createTerm(User currentUser, String artifactKey, long mentionStart, long mentionEnd, String sign, String conceptLabel) {
        Term term = new Term(sign, MODEL_KEY, conceptLabel);
        TermMention termMention = new TermMention()
                .setArtifactKey(artifactKey)
                .setMentionStart(mentionStart)
                .setMentionEnd(mentionEnd)
                .setAuthor(currentUser.getUsername())
                .setDate(new Date());
        term.addTermMention(termMention);
        return new TermAndTermMention(term, termMention);
    }

    private static UcdObject createObject(String objectRowKey, Statement statement) {
        UcdObject ucdObject = new UcdObject(objectRowKey);
        UcdObjectObjectStatement ucdObjectObjectStatement = new UcdObjectObjectStatement();
        ucdObjectObjectStatement.set(statement.getRowKey().toString(), statement.getRowKey().toString());
        ucdObject.addObjectStatement(ucdObjectObjectStatement);
        return ucdObject;
    }

    private static Statement createIsAnObjectStatement(User currentUser, String artifactKey, String sentenceRowKey, String objectRowKey, Term term) {
        StatementRowKey statementRowKey = new StatementRowKey(term.getRowKey(), PredicateRowKey.IS_AN_OBJECT, new TermRowKey(objectRowKey));
        Statement statement = new Statement(statementRowKey);
        StatementArtifact statementArtifact = new StatementArtifact()
                .setArtifactKey(artifactKey)
                .setAuthor(currentUser.getUsername())
                .setDate(new Date())
                .setSentence(sentenceRowKey);
        statement.addStatementArtifact(statementArtifact);
        return statement;
    }

    private static TermAndTermMention createObjectTerm(User currentUser, String artifactKey, String objectSign, String objectConceptLabel, long mentionStart, long mentionEnd) {
        Term objectTerm = new Term(objectSign, "object", objectConceptLabel);
        TermMention objectTermMention = new TermMention()
                .setArtifactKey(artifactKey)
                .setMentionStart(mentionStart)
                .setMentionEnd(mentionEnd)
                .setAuthor(currentUser.getUsername())
                .setDate(new Date());
        objectTerm.addTermMention(objectTermMention);
        return new TermAndTermMention(objectTerm, objectTermMention);
    }

    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
