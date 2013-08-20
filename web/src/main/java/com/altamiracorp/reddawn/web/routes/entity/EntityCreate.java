package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.entityHighlight.TermAndTermMentionOffsetItem;
import com.altamiracorp.reddawn.model.GraphSession;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.graph.GraphVertexImpl;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.VertexType;
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
import com.altamiracorp.web.utils.UrlUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

public class EntityCreate implements Handler, AppAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityCreate.class.getName());
    private static final String MODEL_KEY = "manual";
    private WebApp app;
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private TermRepository termRepository = new TermRepository();
    private OntologyRepository ontologyRepository = new OntologyRepository();
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
        String conceptId = getRequiredParameter(request, "conceptId");
        GraphVertex conceptVertex = graphRepository.findVertex(session.getGraphSession(), conceptId);

        // optional parameters
        String objectSign = request.getParameter("objectSign");

        GraphVertex resolvedNode = null;
        if (objectSign != null && objectSign.length() > 0) {
            objectSign = UrlUtils.urlDecode(objectSign);
            resolvedNode = getObjectGraphNode(session.getGraphSession(), objectSign, conceptVertex);
        }
        TermAndTermMention termAndTermMention = getTermAndTermMention(currentUser, session, artifactKey, mentionStart, mentionEnd, sign, conceptVertex, resolvedNode);

        if (resolvedNode != null) {
            graphRepository.saveRelationship(session.getGraphSession(), termAndTermMention.getTermMention().getGraphVertexId(), resolvedNode.getId(), "isA");
        }

        artifactRepository.touchRow(session.getModelSession(), new ArtifactRowKey(artifactKey));

        TermAndTermMentionOffsetItem offsetItem = new TermAndTermMentionOffsetItem(termAndTermMention);
        new Responder(response).respondWith(offsetItem.toJson());
    }

    private GraphVertex getObjectGraphNode(GraphSession session, String title, GraphVertex conceptVertex) {
        GraphVertex graphNode = graphRepository.findVertexByTitleAndType(session, title, VertexType.ENTITY);
        if (graphNode == null) {
            graphNode = new GraphVertexImpl()
                    .setProperty(PropertyName.TITLE.toString(), title)
                    .setProperty(PropertyName.TYPE.toString(), VertexType.ENTITY.toString())
                    .setProperty(PropertyName.SUBTYPE.toString(), conceptVertex.getId())
                    .setProperty(PropertyName.SOURCE.toString(), "Analyst Resolved Entity");
            String graphNodeId = graphRepository.saveVertex(session, graphNode);
            return new GraphVertexImpl(graphNodeId)
                    .setProperty(PropertyName.TITLE.toString(), title)
                    .setProperty(PropertyName.TYPE.toString(), VertexType.ENTITY.toString())
                    .setProperty(PropertyName.SUBTYPE.toString(), conceptVertex.getId())
                    .setProperty(PropertyName.SOURCE.toString(), "Analyst Resolved Entity");
        }
        return graphNode;
    }

    private TermAndTermMention getTermAndTermMention(User currentUser, RedDawnSession session, String artifactKey, long mentionStart, long mentionEnd, String sign, GraphVertex conceptVertex, GraphVertex resolvedNode) {
        String conceptLabel = StringUtils.join(ontologyRepository.getConceptPath(session.getGraphSession(), conceptVertex.getId()), "/");
        TermRowKey termRowKey = getTermRowKey(session, sign, conceptLabel);
        TermAndTermMention termAndTermMention = termRepository.findMention(session.getModelSession(), termRowKey, artifactKey, mentionStart, mentionEnd);
        if (termAndTermMention == null) {
            LOGGER.info("No existing term mention found... Creating... artifactKey: " + artifactKey);
            Term term = new Term(termRowKey);
            TermMention termMention = new TermMention()
                    .setArtifactKey(artifactKey)
                    .setMentionStart(mentionStart)
                    .setMentionEnd(mentionEnd)
                    .setAuthor(currentUser.getUsername())
                    .setDate(new Date());
            term.addTermMention(termMention);
            termAndTermMention = new TermAndTermMention(term, termMention);
            termRepository.saveToGraph(session.getModelSession(), session.getGraphSession(), termAndTermMention.getTerm(), termAndTermMention.getTermMention(), conceptVertex.getId());
            LOGGER.info("New graph node for term mention created with node id: " + termAndTermMention.getTermMention().getGraphVertexId());
        }

        termAndTermMention.getTermMention().setGraphSubTypeVertexId(conceptVertex.getId());

        if (resolvedNode != null) {
            termAndTermMention.getTermMention().setResolvedGraphVertexId(resolvedNode.getId());
        }
        termRepository.save(session.getModelSession(), termAndTermMention.getTerm());
        return termAndTermMention;
    }

    private TermRowKey getTermRowKey(RedDawnSession session, String sign, String conceptLabel) {
        List<Term> results = termRepository.findByRowKeyRegex(session.getModelSession(), "^" + sign
                + RowKeyHelper.MINOR_FIELD_SEPARATOR + ".*" + RowKeyHelper.MINOR_FIELD_SEPARATOR + conceptLabel + "$");
        if (results.size() > 0) {
            return results.get(0).getRowKey();
        }
        return new TermRowKey(sign, MODEL_KEY, conceptLabel);
    }

    public static String getRequiredParameter(HttpServletRequest request, String parameterName) {
        String parameter = request.getParameter(parameterName);
        if (parameter == null) {
            throw new RuntimeException("'" + parameterName + "' is required.");
        }
        return UrlUtils.urlDecode(parameter);
    }

    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
