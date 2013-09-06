package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.entityHighlight.EntityHighlightWorker;
import com.altamiracorp.reddawn.entityHighlight.TermMentionOffsetItem;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.graph.InMemoryGraphVertex;
import com.altamiracorp.reddawn.model.ontology.LabelName;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.altamiracorp.reddawn.model.termMention.TermMention;
import com.altamiracorp.reddawn.model.termMention.TermMentionRepository;
import com.altamiracorp.reddawn.model.termMention.TermMentionRowKey;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
import com.google.common.util.concurrent.MoreExecutors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EntityTermCreate implements Handler, AppAware {
    private WebApp app;
    private TermMentionRepository termMentionRepository = new TermMentionRepository();
    private GraphRepository graphRepository = new GraphRepository();

    private final ExecutorService executorService = MoreExecutors.getExitingExecutorService(
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()),
            0L, TimeUnit.MILLISECONDS);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);

        // required parameters
        String artifactKey = getRequiredParameter(request, "artifactKey");
        String artifactId = getRequiredParameter(request, "artifactId");
        long mentionStart = Long.parseLong(getRequiredParameter(request, "mentionStart"));
        long mentionEnd = Long.parseLong(getRequiredParameter(request, "mentionEnd"));
        String sign = getRequiredParameter(request, "sign");
        String conceptId = getRequiredParameter(request, "conceptId");
        String resolvedGraphVertexId = request.getParameter("graphVertexId");

        TermMentionRowKey termMentionRowKey = new TermMentionRowKey(artifactKey, mentionStart, mentionEnd);

        GraphVertex conceptVertex = graphRepository.findVertex(session.getGraphSession(), conceptId);
        GraphVertex resolvedVertex;
        if (resolvedGraphVertexId != null) {
            resolvedVertex = graphRepository.findVertex(session.getGraphSession(), resolvedGraphVertexId);
        } else {
            resolvedVertex = graphRepository.findVertexByTitleAndType(session.getGraphSession(), sign, VertexType.ENTITY);
            if (resolvedVertex == null) {
                resolvedVertex = new InMemoryGraphVertex();
                resolvedVertex.setType(VertexType.ENTITY);
            }
            resolvedVertex.setProperty(PropertyName.ROW_KEY, termMentionRowKey.toString());
        }

        resolvedVertex.setProperty(PropertyName.SUBTYPE, conceptVertex.getId());
        resolvedVertex.setProperty(PropertyName.TITLE, sign);

        graphRepository.saveVertex(session.getGraphSession(), resolvedVertex);

        graphRepository.saveRelationship(session.getGraphSession(), artifactId, resolvedVertex.getId(), LabelName.HAS_ENTITY);

        TermMention termMention = termMentionRepository.findByRowKey(session.getModelSession(), termMentionRowKey.toString());
        if (termMention == null) {
            termMention = new TermMention(termMentionRowKey);
        }
        termMention.getMetadata()
                .setSign(sign)
                .setConcept((String) conceptVertex.getProperty(PropertyName.DISPLAY_NAME))
                .setConceptGraphVertexId(conceptVertex.getId())
                .setGraphVertexId(resolvedVertex.getId());
        termMentionRepository.save(session.getModelSession(), termMention);

        // Modify the highlighted artifact text in a background thread
        executorService.execute(new EntityHighlightWorker(session, artifactKey));

        TermMentionOffsetItem offsetItem = new TermMentionOffsetItem(termMention);
        new Responder(response).respondWith(offsetItem.toJson());
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
