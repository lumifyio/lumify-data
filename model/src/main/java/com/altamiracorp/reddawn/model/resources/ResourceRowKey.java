package com.altamiracorp.reddawn.model.resources;

import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.model.RowKeyHelper;

public class ResourceRowKey extends RowKey {
    public ResourceRowKey(String rowKey) {
        super(rowKey);
    }

    public ResourceRowKey(byte[] data) {
        this(RowKeyHelper.buildSHA256KeyStringNoUrn(data));
    }
}
