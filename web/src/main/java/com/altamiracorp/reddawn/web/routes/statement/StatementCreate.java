package com.altamiracorp.reddawn.web.routes.statement;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.entityHighlight.TermAndTermMentionOffsetItem;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import com.altamiracorp.reddawn.ucd.object.UcdObject;
import com.altamiracorp.reddawn.ucd.object.UcdObjectObjectStatement;
import com.altamiracorp.reddawn.ucd.object.UcdObjectRepository;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;
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
import java.net.URLDecoder;
import java.util.Date;

public class StatementCreate implements Handler, AppAware {
    private static final String MODEL_KEY = "manual";
    private WebApp app;
    private StatementRepository statementRepository = new StatementRepository();
    private SentenceRepository sentenceRepository = new SentenceRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User currentUser = User.getUser(request);
        RedDawnSession session = app.getRedDawnSession(request);

        // validate parameters
        String subjectRowKey = request.getParameter("subjectKey");
        String objectRowKey = request.getParameter("objectKey");
        String predicateLabel = request.getParameter("predicateLabel");
        String sentenceRowKey = request.getParameter("sentenceRowKey");

        if (subjectRowKey == null) {
            throw new RuntimeException("'subjectRowKey' is required.");
        }
        subjectRowKey = URLDecoder.decode(subjectRowKey, "UTF-8");

        if (objectRowKey == null) {
            throw new RuntimeException("'objectRowKey' is required.");
        }
        objectRowKey = URLDecoder.decode(objectRowKey, "UTF-8");

        if (predicateLabel == null) {
            throw new RuntimeException("'predicateLabel' is required.");
        }

        if (sentenceRowKey == null) {
            throw new RuntimeException("'sentenceRowKey' is required.");
        }
        sentenceRowKey = URLDecoder.decode(sentenceRowKey, "UTF-8");

        Sentence containingSentence = sentenceRepository.findByRowKey(session.getModelSession(), sentenceRowKey);
        if(containingSentence == null) {
            throw new RuntimeException("The sentence row key given was not found.");
        }

        Statement statement = createStatement(subjectRowKey, predicateLabel, objectRowKey, containingSentence);
        statementRepository.save(session.getModelSession(), statement);

        new Responder(response).respondWith(statement.toJson());
    }

    private static Statement createStatement(String subjectRowKey, String predicateLabel, String objectRowKey, Sentence containingSentence) {
        ArtifactType artifactType = ArtifactType.DOCUMENT;
        if(containingSentence.getMetadata().getArtifactType().equalsIgnoreCase("image")) {
            artifactType = ArtifactType.IMAGE;
        } else if(containingSentence.getMetadata().getArtifactType().equalsIgnoreCase("video")) {
            artifactType = ArtifactType.VIDEO;
        }

        Statement statement = new Statement(new StatementRowKey(new TermRowKey(subjectRowKey), new PredicateRowKey(PredicateRowKey.MANUAL_MODEL_KEY, predicateLabel), new TermRowKey(objectRowKey)));
        StatementArtifact statementArtifact = new StatementArtifact()
                .setArtifactKey(containingSentence.getData().getArtifactId())
                .setArtifactType(artifactType)
                .setArtifactSubject(containingSentence.getMetadata().getArtifactSubject())
                .setSentence(containingSentence.getRowKey())
                .setSentenceText(containingSentence.getData().getText())
                .setDate(new Date())
                .setAuthor(StatementArtifact.MANUAL_AUTHOR)
                .setExtractorId(StatementArtifact.MANUAL_AUTHOR)
                .setSecurityMarking("U");
        statement.addStatementArtifact(statementArtifact);
        return statement;
    }

    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
