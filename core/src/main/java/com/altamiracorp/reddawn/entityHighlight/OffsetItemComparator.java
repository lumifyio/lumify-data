package com.altamiracorp.reddawn.entityHighlight;

import java.util.Comparator;

public class OffsetItemComparator implements Comparator<OffsetItem> {
    @Override
    public int compare(OffsetItem o1, OffsetItem o2) {
        if (o1.getStart() == o2.getStart()) {
            if (o1.getEnd().longValue() == o2.getEnd().longValue()) {
                return 0;
            }
            return o1.getEnd() < o2.getEnd() ? -1 : 1;
        }
        return o1.getStart() < o2.getStart() ? -1 : 1;
    }
}
