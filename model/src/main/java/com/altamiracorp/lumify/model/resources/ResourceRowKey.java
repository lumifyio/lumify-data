package com.altamiracorp.lumify.model.resources;

import com.altamiracorp.lumify.model.RowKey;
import com.altamiracorp.lumify.model.RowKeyHelper;

public class ResourceRowKey extends RowKey {
    public ResourceRowKey(String rowKey) {
        super(rowKey);
    }

    public ResourceRowKey(byte[] data) {
        this(RowKeyHelper.buildSHA256KeyStringNoUrn(data));
    }
}
