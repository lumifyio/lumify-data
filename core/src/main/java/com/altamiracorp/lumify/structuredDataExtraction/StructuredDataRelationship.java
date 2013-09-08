package com.altamiracorp.lumify.structuredDataExtraction;

public class StructuredDataRelationship {
    private TermAndGraphVertex source;
    private TermAndGraphVertex dest;
    private String label;

    public StructuredDataRelationship(TermAndGraphVertex termMentionSource, TermAndGraphVertex termMentionDest, String label) {
        this.source = termMentionSource;
        this.dest = termMentionDest;
        this.label = label;
    }

    public TermAndGraphVertex getSource() {
        return source;
    }

    public TermAndGraphVertex getDest() {
        return dest;
    }

    public String getLabel() {
        return label;
    }
}
