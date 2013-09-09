package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.termMention.TermMention;

public class TermAndGraphVertex {
    private final boolean useExisting;
    private final TermMention termMention;
    private final GraphVertex graphVertex;

    public TermAndGraphVertex(TermMention termMention, GraphVertex graphVertex, boolean useExisting) {
        this.termMention = termMention;
        this.graphVertex = graphVertex;
        this.useExisting = useExisting;
    }

    public TermMention getTermMention() {
        return termMention;
    }

    public GraphVertex getGraphVertex() {
        return graphVertex;
    }

    public boolean isUseExisting() {
        return useExisting;
    }
}
