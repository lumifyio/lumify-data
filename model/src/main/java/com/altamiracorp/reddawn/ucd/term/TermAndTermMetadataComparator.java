package com.altamiracorp.reddawn.ucd.term;

import java.util.Comparator;

public class TermAndTermMetadataComparator implements Comparator<TermAndTermMention> {
    @Override
    public int compare(TermAndTermMention t1, TermAndTermMention t2) {
        long t1Start = t1.getTermMention().getMentionStart();
        long t2Start = t2.getTermMention().getMentionStart();
        if (t1Start == t2Start) {
            long t1End = t1.getTermMention().getMentionEnd();
            long t2End = t2.getTermMention().getMentionEnd();
            if (t1End == t2End) {
                return 0;
            }
            return t1End > t2End ? 1 : -1;
        }
        return t1Start > t2Start ? 1 : -1;
    }
}
