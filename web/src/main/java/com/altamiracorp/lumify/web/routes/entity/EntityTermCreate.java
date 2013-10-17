package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.core.model.ontology.LabelName;
import com.altamiracorp.lumify.core.model.termMention.TermMention;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.storm.textHighlighting.TermMentionOffsetItem;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityTermCreate extends BaseRequestHandler {
    private final EntityHelper entityHelper;
    private final GraphRepository graphRepository;

    @Inject
    public EntityTermCreate(
            final EntityHelper entityHelper,
            final GraphRepository graphRepository) {
        this.entityHelper = entityHelper;
        this.graphRepository = graphRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
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
        entityHelper.scheduleHighlight(artifactId, user);

        respondWithJson(response, offsetItem.toJson());
    }
}
