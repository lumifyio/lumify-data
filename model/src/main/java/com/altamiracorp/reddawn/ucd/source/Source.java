package com.altamiracorp.reddawn.ucd.source;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;

public class Source extends Row<SourceRowKey> {
    public static final String TABLE_NAME = "Source";

    public Source(RowKey rowKey) {
        super(TABLE_NAME, new SourceRowKey(rowKey.toString()));
    }

    public Source(String uuid) {
        super(TABLE_NAME, new SourceRowKey(uuid));
    }

    public SourceMetadata getMetadata() {
        SourceMetadata sourceMetadata = get(SourceMetadata.NAME);
        if (sourceMetadata == null) {
            addColumnFamily(new SourceMetadata());
        }
        return get(SourceMetadata.NAME);
    }
}
