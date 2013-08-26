package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.termMention.TermMention;

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
