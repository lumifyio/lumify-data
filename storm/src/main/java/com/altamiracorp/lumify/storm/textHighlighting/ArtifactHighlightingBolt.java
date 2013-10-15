package com.altamiracorp.lumify.storm.textHighlighting;

import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.tuple.Tuple;

import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.entityHighlight.EntityHighlighter;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.storm.BaseTextProcessingBolt;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.google.inject.Inject;

public class ArtifactHighlightingBolt extends BaseTextProcessingBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactHighlightingBolt.class);
    private TermMentionRepository termMentionRepository;
    private EntityHighlighter entityHighlighter;

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        final JSONObject json = getJsonFromTuple(input);
        final String graphVertexId = json.getString("graphVertexId");

        GraphVertex graphVertex = graphRepository.findVertex(graphVertexId, getUser());
        if( graphVertex != null ) {
            String artifactRowKey = (String) graphVertex.getProperty(PropertyName.ROW_KEY);
            LOGGER.info(String.format("Processing graph vertex [%s] for artifact: %s", graphVertex.getId(), artifactRowKey));

            String text = getText(graphVertex);
            List<TermMention> termMentions = termMentionRepository.findByGraphVertexId(graphVertex.getId(), getUser());
            String highlightedText = entityHighlighter.getHighlightedText(text, termMentions, getUser());

            Artifact artifact = new Artifact(artifactRowKey);
            artifact.getMetadata().setHighlightedText(highlightedText);
            artifactRepository.save(artifact, getUser());

            graphVertex.removeProperty(PropertyName.HIGHLIGHTED_TEXT_HDFS_PATH.toString());
            graphRepository.save(graphVertex, getUser());
        } else {
            LOGGER.warn("Could not find vertex with id: " + graphVertexId);
        }

        getCollector().ack(input);
    }

    @Inject
    public void setEntityHighlighter(EntityHighlighter entityHighlighter) {
        this.entityHighlighter = entityHighlighter;
    }

    @Inject
    public void setTermMentionRepository(TermMentionRepository termMentionRepository) {
        this.termMentionRepository = termMentionRepository;
    }
}
