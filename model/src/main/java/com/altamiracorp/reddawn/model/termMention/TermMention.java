package com.altamiracorp.reddawn.model.termMention;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;

public class TermMention extends Row<TermMentionRowKey> {
    public static final String TABLE_NAME = "atc_TermMention";

    public TermMention(TermMentionRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public TermMention() {
        super(TABLE_NAME);
    }

    public TermMention(RowKey rowKey) {
        this(new TermMentionRowKey(rowKey.toString()));
    }

    public TermMentionMetadata getMetadata() {
        TermMentionMetadata termMentionMetadata = get(TermMentionMetadata.NAME);
        if (termMentionMetadata == null) {
            addColumnFamily(new TermMentionMetadata());
        }
        return get(TermMentionMetadata.NAME);
    }
}
