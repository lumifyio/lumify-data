package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityHighlight.EntityHighlightWorker;
import com.altamiracorp.lumify.entityHighlight.EntityHighlighter;
import com.altamiracorp.lumify.entityHighlight.TermMentionOffsetItem;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.LabelName;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityTermUpdate extends BaseRequestHandler {
    private final TermMentionRepository termMentionRepository;
    private final GraphRepository graphRepository;
    private final ArtifactRepository artifactRepository;
    private final EntityHighlighter highlighter;

    @Inject
    public EntityTermUpdate(
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
        final String resolvedGraphVertexId = getRequiredParameter(request, "graphVertexId");

        User user = getUser(request);
        GraphVertex conceptVertex = graphRepository.findVertex(conceptId, user);
        GraphVertex resolvedVertex = graphRepository.findVertex(resolvedGraphVertexId, user);
        entityHelper.updateGraphVertex(resolvedVertex, conceptId, sign, user);

        if (graphRepository.findEdge(artifactId, resolvedGraphVertexId, LabelName.HAS_ENTITY.toString(), user) == null ){
            graphRepository.saveRelationship(artifactId, resolvedVertex.getId(), LabelName.HAS_ENTITY, user);
        }

        TermMentionRowKey termMentionRowKey = new TermMentionRowKey(artifactKey, mentionStart, mentionEnd);
        TermMention termMention = termMentionRepository.findByRowKey(termMentionRowKey.toString(), user);
        if (termMention == null) {
            termMention = new TermMention(termMentionRowKey);
        }
        entityHelper.updateTermMention(termMention, sign, conceptVertex, resolvedVertex, user);

        entityHelper.executeService(new EntityHighlightWorker(artifactRepository, highlighter, artifactKey, user));

        TermMentionOffsetItem offsetItem = new TermMentionOffsetItem(termMention, resolvedVertex);
        respondWithJson(response, offsetItem.toJson());
    }
}
