package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.securegraph.Vertex;

public class TermMentionWithGraphVertex {
    private final TermMentionModel termMention;
    private final Vertex vertex;

    public TermMentionWithGraphVertex(TermMentionModel termMention, Vertex vertex) {
        this.termMention = termMention;
        this.vertex = vertex;
    }

    public TermMentionModel getTermMention() {
        return termMention;
    }

    public Vertex getVertex() {
        return vertex;
    }
}
