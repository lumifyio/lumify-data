package com.altamiracorp.lumify.web.routes.entity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityHighlight.EntityHighlightWorker;
import com.altamiracorp.lumify.entityHighlight.EntityHighlighter;
import com.altamiracorp.lumify.entityHighlight.TermMentionOffsetItem;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.model.ontology.LabelName;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class EntityTermCreate extends BaseRequestHandler {
    private final TermMentionRepository termMentionRepository;
    private final GraphRepository graphRepository;
    private final ArtifactRepository artifactRepository;
    private final EntityHighlighter highlighter;

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

        final GraphVertex createdVertex = new InMemoryGraphVertex();
        createdVertex.setType(VertexType.ENTITY);
        createdVertex.setProperty(PropertyName.ROW_KEY, termMentionRowKey.toString());
        entityHelper.updateGraphVertex(createdVertex, conceptId, sign, user);
        graphRepository.saveRelationship(artifactId, createdVertex.getId(), LabelName.HAS_ENTITY, user);

        TermMention termMention = new TermMention(termMentionRowKey);
        entityHelper.updateTermMention(termMention, sign, conceptVertex, createdVertex, user);
        TermMentionOffsetItem offsetItem = new TermMentionOffsetItem(termMention, createdVertex);

        // Modify the highlighted artifact text in a background thread
        entityHelper.executeService(new EntityHighlightWorker(artifactRepository, highlighter, artifactKey, user));

        respondWithJson(response, offsetItem.toJson());
    }
}
