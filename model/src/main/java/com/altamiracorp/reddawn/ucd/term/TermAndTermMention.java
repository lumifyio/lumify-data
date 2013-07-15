package com.altamiracorp.reddawn.ucd.term;

public class TermAndTermMention {
    private Term term;
    private TermMention termMention;

    public TermAndTermMention(Term term, TermMention termMetadata) {
        this.term = term;
        this.termMention = termMetadata;
    }

    public Term getTerm() {
        return term;
    }

    public TermMention getTermMention() {
        return termMention;
    }

    @Override
    public String toString() {
        return getTerm().getRowKey().getSign()
                + " - "
                + getTermMention().getMentionStart()
                + "-"
                + getTermMention().getMentionEnd();
    }
}
