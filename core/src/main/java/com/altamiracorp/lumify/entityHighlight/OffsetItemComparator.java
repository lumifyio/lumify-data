package com.altamiracorp.lumify.entityHighlight;

import java.util.Comparator;

public class OffsetItemComparator implements Comparator<OffsetItem> {
    @Override
    public int compare(OffsetItem o1, OffsetItem o2) {
        if (o1.getStart() == o2.getStart()) {
            if (o1.getEnd() == o2.getEnd()) {
                if (o1 instanceof TermMentionOffsetItem && o2 instanceof TermMentionOffsetItem) {
                    TermMentionOffsetItem ttmoi1 = (TermMentionOffsetItem) o1;
                    TermMentionOffsetItem ttmoi2 = (TermMentionOffsetItem) o2;
                    return ttmoi1.compareTo(ttmoi2);
                }
                return 0;
            }
            return o1.getEnd() < o2.getEnd() ? -1 : 1;
        }
        return o1.getStart() < o2.getStart() ? -1 : 1;
    }
}
