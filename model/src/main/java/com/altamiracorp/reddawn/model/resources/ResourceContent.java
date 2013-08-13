package com.altamiracorp.reddawn.model.resources;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Value;

public class ResourceContent extends ColumnFamily {
    public static final String NAME = "Content";
    private static final String DATA = "data";
    private static final String CONTENT_TYPE = "contentType";

    public ResourceContent() {
        super(NAME);
    }

    public ResourceContent setData(byte[] data) {
        set(DATA, data);
        return this;
    }

    public byte[] getData() {
        return Value.toBytes(get(DATA));
    }

    public ResourceContent setContentType(String contentType) {
        set(CONTENT_TYPE, contentType);
        return this;
    }

    public String getContentType() {
        return Value.toString(get(CONTENT_TYPE));
    }
}
