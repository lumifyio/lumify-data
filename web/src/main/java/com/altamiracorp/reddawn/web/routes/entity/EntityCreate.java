package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.entityHighlight.TermAndTermMentionOffsetItem;
import com.altamiracorp.reddawn.model.GraphSession;
import com.altamiracorp.reddawn.model.graph.GraphNode;
import com.altamiracorp.reddawn.model.graph.GraphNodeImpl;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
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
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private TermRepository termRepository = new TermRepository();
    private GraphRepository graphRepository = new GraphRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User currentUser = User.getUser(request);
        RedDawnSession session = app.getRedDawnSession(request);

        // required parameters
        String artifactKey = getRequiredParameter(request, "artifactKey");
        long mentionStart = Long.parseLong(getRequiredParameter(request, "mentionStart"));
        long mentionEnd = Long.parseLong(getRequiredParameter(request, "mentionEnd"));
        String sign = getRequiredParameter(request, "sign");
        String conceptLabel = getRequiredParameter(request, "conceptLabel");

        // optional parameters
        String objectSign = request.getParameter("objectSign");

        GraphNode resolvedNode = null;
        String resolvedGraphNodeId = null;
        if (objectSign != null && objectSign.length() > 0) {
            resolvedNode = getObjectGraphNode(session.getGraphSession(), objectSign, conceptLabel);
            resolvedGraphNodeId = resolvedNode.getId();
        }
        TermAndTermMention termAndTermMention = getTermAndTermMention(currentUser, session, artifactKey, mentionStart, mentionEnd, sign, conceptLabel, resolvedGraphNodeId);

        if (resolvedNode != null) {
            graphRepository.saveRelationship(session.getGraphSession(), resolvedGraphNodeId, termAndTermMention.getTermMention().getGraphNodeId(), "entityResolved");
        }

        artifactRepository.touchRow(session.getModelSession(), new ArtifactRowKey(artifactKey));

        TermAndTermMentionOffsetItem offsetItem = new TermAndTermMentionOffsetItem(termAndTermMention);
        new Responder(response).respondWith(offsetItem.toJson());
    }

    private GraphNode getObjectGraphNode(GraphSession session, String title, String subType) {
        GraphNode graphNode = graphRepository.findNodeByTitleAndType(session, title, GraphRepository.ENTITY_TYPE);
        if (graphNode == null) {
            graphNode = new GraphNodeImpl()
                    .setProperty("title", title)
                    .setProperty("type", GraphRepository.ENTITY_TYPE)
                    .setProperty("subType", subType);
            String graphNodeId = graphRepository.saveNode(session, graphNode);
            return new GraphNodeImpl(graphNodeId).setProperty("title", title).setProperty("type", GraphRepository.ENTITY_TYPE).setProperty("subType", subType);
        }
        return graphNode;
    }

    private TermAndTermMention getTermAndTermMention(User currentUser, RedDawnSession session, String artifactKey, long mentionStart, long mentionEnd, String sign, String conceptLabel, String resolvedNodeId) {
        TermRowKey termRowKey = new TermRowKey(sign, MODEL_KEY, conceptLabel);
        TermAndTermMention termAndTermMention = termRepository.findMention(session.getModelSession(), termRowKey, artifactKey, mentionStart, mentionEnd);
        if (termAndTermMention == null) {
            Term term = new Term(termRowKey);
            TermMention termMention = new TermMention()
                    .setArtifactKey(artifactKey)
                    .setMentionStart(mentionStart)
                    .setMentionEnd(mentionEnd)
                    .setAuthor(currentUser.getUsername())
                    .setDate(new Date());
            if (resolvedNodeId != null) {
                termMention.setResolvedGraphNodeId(resolvedNodeId);
            }
            term.addTermMention(termMention);
            termAndTermMention = new TermAndTermMention(term, termMention);

            termRepository.save(session.getModelSession(), termAndTermMention.getTerm());
            termRepository.saveToGraph(session.getModelSession(), session.getGraphSession(), termAndTermMention.getTerm(), termAndTermMention.getTermMention());
        }

        return termAndTermMention;
    }

    public static String getRequiredParameter(HttpServletRequest request, String parameterName) {
        String parameter = request.getParameter(parameterName);
        if (parameter == null) {
            throw new RuntimeException("'" + parameterName + "' is required.");
        }
        return parameter;
    }

    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
