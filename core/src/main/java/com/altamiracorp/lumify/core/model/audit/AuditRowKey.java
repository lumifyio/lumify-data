package com.altamiracorp.lumify.core.model.audit;

import com.altamiracorp.bigtable.model.RowKey;
import com.altamiracorp.lumify.core.util.RowKeyHelper;

public class AuditRowKey extends RowKey {
    public AuditRowKey(String rowKey) {
        super(rowKey);
    }

    public static AuditRowKey build(String vertexId) {
        return new AuditRowKey(RowKeyHelper.buildMinor(vertexId, RowKeyHelper.padLong(System.currentTimeMillis())));
    }
}
