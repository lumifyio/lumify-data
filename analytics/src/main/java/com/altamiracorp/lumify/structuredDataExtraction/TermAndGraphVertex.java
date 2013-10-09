package com.altamiracorp.lumify.structuredDataExtraction;

import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.termMention.TermMention;

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
