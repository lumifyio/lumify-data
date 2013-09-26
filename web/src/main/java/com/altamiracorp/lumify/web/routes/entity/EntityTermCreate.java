package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityHighlight.EntityHighlightWorker;
import com.altamiracorp.lumify.entityHighlight.EntityHighlighter;
import com.altamiracorp.lumify.entityHighlight.TermMentionOffsetItem;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.ontology.VertexType;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EntityTermCreate extends BaseRequestHandler {
    private final TermMentionRepository termMentionRepository;
    private final GraphRepository graphRepository;
    private final ArtifactRepository artifactRepository;
    private final EntityHighlighter highlighter;

    private final ExecutorService executorService = MoreExecutors.getExitingExecutorService(
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()),
            0L, TimeUnit.MILLISECONDS);

    @Inject
    public EntityTermCreate(
            final TermMentionRepository termMentionRepository,
            final GraphRepository graphRepository,
            final ArtifactRepository artifactRepository,
            final EntityHighlighter highlighter) {
        this.termMentionRepository = termMentionRepository;
        this.graphRepository = graphRepository;
        this.artifactRepository = artifactRepository;
        this.highlighter = highlighter;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        EntityHelper entityHelper = new EntityHelper(termMentionRepository, graphRepository);

        // required parameters
        final String artifactKey = getRequiredParameter(request, "artifactKey");
        final String artifactId = getRequiredParameter(request, "artifactId");
        final long mentionStart = getRequiredParameterAsLong(request, "mentionStart");
        final long mentionEnd = getRequiredParameterAsLong(request, "mentionEnd");
        final String sign = getRequiredParameter(request, "sign");
        final String conceptId = getRequiredParameter(request, "conceptId");

        User user = getUser(request);
        TermMentionRowKey termMentionRowKey = new TermMentionRowKey(artifactKey, mentionStart, mentionEnd);

        GraphVertex conceptVertex = graphRepository.findVertex(conceptId, user);
        GraphVertex resolvedVertex = graphRepository.findVertexByTitleAndType(sign, VertexType.ENTITY, user);
        if (resolvedVertex == null) {
            resolvedVertex = new InMemoryGraphVertex();
            resolvedVertex.setType(VertexType.ENTITY);
        }
        resolvedVertex.setProperty(PropertyName.ROW_KEY, termMentionRowKey.toString());
        entityHelper.updateGraphVertex(resolvedVertex, conceptId, sign, user);

        TermMention termMention = new TermMention(termMentionRowKey);

        entityHelper.updateTermMention(termMention, sign, conceptVertex, resolvedVertex, user);

        // Modify the highlighted artifact text in a background thread
        entityHelper.executeService(new EntityHighlightWorker(artifactRepository, highlighter, artifactKey, user));

        TermMentionOffsetItem offsetItem = new TermMentionOffsetItem(termMention, resolvedVertex);

        respondWithJson(response, offsetItem.toJson());
    }
}
