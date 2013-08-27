package com.altamiracorp.reddawn.web.routes.entity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.altamiracorp.reddawn.model.graph.InMemoryGraphVertex;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.entityHighlight.EntityHighlightWorker;
import com.altamiracorp.reddawn.entityHighlight.TermAndTermMentionOffsetItem;
import com.altamiracorp.reddawn.model.GraphSession;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;
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
import com.altamiracorp.web.utils.UrlUtils;
import com.google.common.util.concurrent.MoreExecutors;

public class EntityCreate implements Handler, AppAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityCreate.class.getName());
    private static final String MODEL_KEY = "manual";
    private WebApp app;
    private TermRepository termRepository = new TermRepository();
    private OntologyRepository ontologyRepository = new OntologyRepository();
    private GraphRepository graphRepository = new GraphRepository();

    private final ExecutorService executorService = MoreExecutors.getExitingExecutorService(
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()),
            0L, TimeUnit.MILLISECONDS);

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
        String graphVertexId = request.getParameter("graphVertexId");

        // optional parameters
        String objectSign = request.getParameter("objectSign");

        GraphVertex conceptVertex = graphRepository.findVertex(session.getGraphSession(), conceptId);
        GraphVertex resolvedVertex = null;
        if (objectSign != null && objectSign.length() > 0) {
            objectSign = UrlUtils.urlDecode(objectSign);
            resolvedVertex = getObjectGraphVertex(session.getGraphSession(), objectSign, conceptVertex);
        }
        TermAndTermMention termAndTermMention = getTermAndTermMention(currentUser, session, artifactKey, mentionStart, mentionEnd, sign, conceptVertex, resolvedVertex, graphVertexId);

        if (resolvedVertex != null) {
            graphRepository.saveRelationship(session.getGraphSession(), termAndTermMention.getTermMention().getGraphVertexId(), resolvedVertex.getId(), "isA");
        }

        // Modify the highlighted artifact text in a background thread
        executorService.execute(new EntityHighlightWorker(session, artifactKey));

        TermAndTermMentionOffsetItem offsetItem = new TermAndTermMentionOffsetItem(termAndTermMention);
        new Responder(response).respondWith(offsetItem.toJson());
    }

    private GraphVertex getObjectGraphVertex(GraphSession session, String title, GraphVertex conceptVertex) {
        GraphVertex graphVertex = graphRepository.findVertexByTitleAndType(session, title, VertexType.ENTITY);
        if (graphVertex == null) {
            graphVertex = new InMemoryGraphVertex()
                    .setProperty(PropertyName.TITLE.toString(), title)
                    .setProperty(PropertyName.TYPE.toString(), VertexType.ENTITY.toString())
                    .setProperty(PropertyName.SUBTYPE.toString(), conceptVertex.getId())
                    .setProperty(PropertyName.SOURCE.toString(), "Analyst Resolved Entity");
            String graphVertexId = graphRepository.saveVertex(session, graphVertex);
            return new InMemoryGraphVertex(graphVertexId)
                    .setProperty(PropertyName.TITLE.toString(), title)
                    .setProperty(PropertyName.TYPE.toString(), VertexType.ENTITY.toString())
                    .setProperty(PropertyName.SUBTYPE.toString(), conceptVertex.getId())
                    .setProperty(PropertyName.SOURCE.toString(), "Analyst Resolved Entity");
        }
        return graphVertex;
    }

    private TermAndTermMention getTermAndTermMention(User currentUser, RedDawnSession session, String artifactKey, long mentionStart, long mentionEnd, String sign, GraphVertex conceptVertex, GraphVertex resolvedVertex, String graphVertexId) {
        String conceptLabel = StringUtils.join(ontologyRepository.getConceptPath(session.getGraphSession(), conceptVertex.getId()), "/");
        TermRowKey termRowKey;
        GraphVertex vertex = graphRepository.findVertex(session.getGraphSession(), graphVertexId);
        if (vertex != null && vertex.getProperty(PropertyName.ROW_KEY) != null) {
            termRowKey = new TermRowKey(vertex.getProperty(PropertyName.ROW_KEY).toString());
        }
        else {
            termRowKey = getTermRowKey(session, sign, conceptLabel);
        }
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
            LOGGER.info("New graph vertex for term mention created with vertex id: " + termAndTermMention.getTermMention().getGraphVertexId());
        }

        termAndTermMention.getTermMention().setGraphSubTypeVertexId(conceptVertex.getId());

        if (resolvedVertex != null) {
            termAndTermMention.getTermMention().setResolvedGraphVertexId(resolvedVertex.getId());
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

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
