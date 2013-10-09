package com.altamiracorp.lumify.entityExtraction;

import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.termMention.TermMention;

public class ExtractedEntity {
    private final TermMention termMention;
    private final GraphVertex graphVertex;

    public ExtractedEntity(TermMention termMention, GraphVertex graphVertex) {
        this.termMention = termMention;
        this.graphVertex = graphVertex;
    }

    public TermMention getTermMention() {
        return termMention;
    }

    public GraphVertex getGraphVertex() {
        return graphVertex;
    }
}
