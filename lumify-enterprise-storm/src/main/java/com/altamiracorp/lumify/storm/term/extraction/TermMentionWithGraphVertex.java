package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;

public class TermMentionWithGraphVertex {
    private final TermMentionModel termMention;
    private final GraphVertex graphVertex;

    public TermMentionWithGraphVertex(TermMentionModel termMention, GraphVertex graphVertex) {
        this.termMention = termMention;
        this.graphVertex = graphVertex;
    }

    public TermMentionModel getTermMention() {
        return termMention;
    }

    public GraphVertex getGraphVertex() {
        return graphVertex;
    }
}
