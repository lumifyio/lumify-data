package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.ucd.object.UcdObjectRowKey;

public abstract class OffsetItem {
    public abstract Long getStart();

    public abstract Long getEnd();

    public abstract String getType();

    public String getSubType() {
        return null;
    }

    public abstract String getRowKey();

    public String getConceptLabel() {
        return null;
    }

    public UcdObjectRowKey getObjectRowKey() {
        return null;
    }
}
