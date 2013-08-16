package com.altamiracorp.reddawn.entityHighlight;

import java.util.Comparator;

public class OffsetItemComparator implements Comparator<OffsetItem> {
    @Override
    public int compare(OffsetItem o1, OffsetItem o2) {
        if (o1.getStart() == o2.getStart()) {
            if (o1 instanceof SentenceOffsetItem && o2 instanceof TermAndTermMentionOffsetItem) {
                return -1;
            }
            if (o1 instanceof TermAndTermMentionOffsetItem && o2 instanceof SentenceOffsetItem) {
                return 1;
            }
            if (o1.getEnd() == o2.getEnd()) {
                if (o1 instanceof TermAndTermMentionOffsetItem && o2 instanceof TermAndTermMentionOffsetItem) {
                    TermAndTermMentionOffsetItem ttmoi1 = (TermAndTermMentionOffsetItem) o1;
                    TermAndTermMentionOffsetItem ttmoi2 = (TermAndTermMentionOffsetItem) o2;
                    return ttmoi1.compareTo(ttmoi2);
                }
                return 0;
            }
            return o1.getEnd() < o2.getEnd() ? -1 : 1;
        }
        return o1.getStart() < o2.getStart() ? -1 : 1;
    }
}
