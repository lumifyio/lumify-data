package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;

public class TermAndGraphVertex {
    private TermAndTermMention termAndTermMention;
    private GraphVertex graphVertex;

    public TermAndGraphVertex(TermAndTermMention termAndTermMention, GraphVertex graphVertex) {
        this.termAndTermMention = termAndTermMention;
        this.graphVertex = graphVertex;
    }

    public TermAndTermMention getTermAndTermMention() {
        return termAndTermMention;
    }

    public GraphVertex getGraphVertex() {
        return graphVertex;
    }
}
