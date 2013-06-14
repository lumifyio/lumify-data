package com.altamiracorp.reddawn.ucd.sentence;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;

public class SentenceTerm extends ColumnFamily {
    public static final String TERM_ID = "termId";

    public SentenceTerm(TermMention termMention) {
        super(termMention.getColumnFamilyName());

    }

    public SentenceTerm setTermId(Term term) {
        setTermId(term.getRowKey().toString());
        return this;
    }

    public SentenceTerm setTermId(String termId) {
        set(TERM_ID, termId);
        return this;
    }
}
