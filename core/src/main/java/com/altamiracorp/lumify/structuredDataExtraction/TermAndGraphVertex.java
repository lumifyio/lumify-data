package com.altamiracorp.lumify.structuredDataExtraction;

import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.termMention.TermMention;

public class TermAndGraphVertex {
    private TermMention termMention;
    private GraphVertex graphVertex;

    public TermAndGraphVertex(TermMention termMention, GraphVertex graphVertex) {
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
