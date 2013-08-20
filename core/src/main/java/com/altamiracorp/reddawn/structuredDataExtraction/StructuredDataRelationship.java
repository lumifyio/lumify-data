package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.ucd.term.TermMention;

public class StructuredDataRelationship {
    private TermMention termMentionSource;
    private TermMention termMentionDest;
    private String label;

    public StructuredDataRelationship(TermMention termMentionSource, TermMention termMentionDest, String label) {
        this.termMentionSource = termMentionSource;
        this.termMentionDest = termMentionDest;
        this.label = label;
    }

    public TermMention getTermMentionSource() {
        return termMentionSource;
    }

    public TermMention getTermMentionDest() {
        return termMentionDest;
    }

    public String getLabel() {
        return label;
    }
}
