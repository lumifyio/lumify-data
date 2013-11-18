package com.altamiracorp.lumify.core.model.audit;

import com.altamiracorp.bigtable.model.Row;
import com.altamiracorp.bigtable.model.RowKey;

public class Audit extends Row<AuditRowKey> {
    public static final String TABLE_NAME = "atc_audit";

    public Audit(RowKey rowKey) {
        super(TABLE_NAME, new AuditRowKey(rowKey.toString()));
    }

    public Audit(String rowKey) {
        super(TABLE_NAME, new AuditRowKey(rowKey));
    }

    public Audit() {
        super(TABLE_NAME);
    }

    public AuditData getData() {
        AuditData auditData = get(AuditData.NAME);
        if (auditData == null) {
            addColumnFamily(new AuditData());
        }
        return get(AuditData.NAME);
    }
}
