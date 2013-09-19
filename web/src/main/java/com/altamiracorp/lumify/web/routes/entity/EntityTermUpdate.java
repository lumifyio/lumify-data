package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.entityHighlight.EntityHighlightWorker;
import com.altamiracorp.lumify.entityHighlight.TermMentionOffsetItem;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityTermUpdate extends BaseRequestHandler {
    private final Repository<TermMention> termMentionRepository;
    private final GraphRepository graphRepository;

    @Inject
    public EntityTermUpdate(final Repository<TermMention> termMentionRepo, final GraphRepository graphRepo) {
        termMentionRepository = termMentionRepo;
        graphRepository = graphRepo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        EntityHelper entityHelper = new EntityHelper(termMentionRepository, graphRepository);
        AppSession session = app.getAppSession(request);

        // required parameters
        final String artifactKey = getRequiredParameter(request, "artifactKey");
        final String artifactId = getRequiredParameter(request, "artifactId");
        final long mentionStart = getRequiredParameterAsLong(request, "mentionStart");
        final long mentionEnd = getRequiredParameterAsLong(request, "mentionEnd");
        final String sign = getRequiredParameter(request, "sign");
        final String conceptId = getRequiredParameter(request, "conceptId");
        final String resolvedGraphVertexId = getRequiredParameter(request, "graphVertexId");

        GraphVertex conceptVertex = graphRepository.findVertex(session.getGraphSession(), conceptId);
        GraphVertex resolvedVertex = graphRepository.findVertex(session.getGraphSession(), resolvedGraphVertexId);
        entityHelper.updateGraphVertex(session, resolvedVertex, conceptId, sign, artifactId);

        TermMentionRowKey termMentionRowKey = new TermMentionRowKey(artifactKey, mentionStart, mentionEnd);
        TermMention termMention = termMentionRepository.findByRowKey(session.getModelSession(), termMentionRowKey.toString());
        entityHelper.updateTermMention(session, termMention, sign, conceptVertex, resolvedVertex);

        entityHelper.executeService(new EntityHighlightWorker(session, artifactKey));

        TermMentionOffsetItem offsetItem = new TermMentionOffsetItem(termMention, resolvedVertex);
        respondWithJson(response, offsetItem.toJson());
    }
}
