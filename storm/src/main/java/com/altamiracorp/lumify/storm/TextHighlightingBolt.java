package com.altamiracorp.lumify.storm;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.entityHighlight.EntityHighlighter;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.google.inject.Inject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TextHighlightingBolt extends BaseTextProcessingBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextHighlightingBolt.class.getName());
    private TermMentionRepository termMentionRepository;
    private EntityHighlighter entityHighlighter;

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        JSONObject json = getJsonFromTuple(input);
        String graphVertexId = json.getString("graphVertexId");
        GraphVertex graphVertex = graphRepository.findVertex(graphVertexId, getUser());
        String text = getText(graphVertex);
        List<TermMention> termMentions = termMentionRepository.findByGraphVertexId(graphVertex.getId(), getUser());
        String highlightedText = entityHighlighter.getHighlightedText(text, termMentions, getUser());

        String artifactRowKey = (String) graphVertex.getProperty(PropertyName.ROW_KEY);
        Artifact artifact = new Artifact(artifactRowKey);
        artifact.getMetadata().setHighlightedText(highlightedText);
        artifactRepository.save(artifact, getUser());

        graphVertex.removeProperty(PropertyName.HIGHLIGHTED_TEXT_HDFS_PATH.toString());
        graphRepository.save(graphVertex, getUser());

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
