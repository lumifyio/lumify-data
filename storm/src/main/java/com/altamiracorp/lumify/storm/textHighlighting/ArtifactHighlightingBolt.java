package com.altamiracorp.lumify.storm.textHighlighting;

import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.tuple.Tuple;

import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.termMention.TermMention;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.storm.BaseTextProcessingBolt;
import com.altamiracorp.lumify.storm.term.analysis.LocationTermAnalyzer;
import com.altamiracorp.lumify.storm.term.extraction.TermMentionWithGraphVertex;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
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

            List<TermMention> termMentions = termMentionRepository.findByGraphVertexId(graphVertex.getId(), getUser());
            performHighlighting(artifactRowKey, graphVertex, termMentions);
            performLocationAnalysis(graphVertex, termMentions);
        } else {
            LOGGER.warn("Could not find vertex with id: " + graphVertexId);
        }

        getCollector().ack(input);
    }

    private void performHighlighting(final String rowKey, final GraphVertex vertex, final List<TermMention> termMentions) throws Exception {
        String text = getText(vertex);
        String highlightedText = entityHighlighter.getHighlightedText(text, termMentions, getUser());

        Artifact artifact = new Artifact(rowKey);
        artifact.getMetadata().setHighlightedText(highlightedText);
        artifactRepository.save(artifact, getUser());

        vertex.removeProperty(PropertyName.HIGHLIGHTED_TEXT_HDFS_PATH.toString());
        graphRepository.save(vertex, getUser());
    }

    private void performLocationAnalysis(final GraphVertex vertex, final List<TermMention> termMentions) {
        final LocationTermAnalyzer locationAnalyzer = getInjector().getInstance(LocationTermAnalyzer.class);

        for(final TermMention mention : termMentions) {
            locationAnalyzer.analyzeTermData(new TermMentionWithGraphVertex(mention, vertex), getUser());
        }
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
