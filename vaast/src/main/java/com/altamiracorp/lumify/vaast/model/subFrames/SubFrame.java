package com.altamiracorp.lumify.vaast.model.subFrames;

import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.RowKey;

public class SubFrame extends Row<SubFrameRowKey> {

    public static final String TABLE_NAME = "atc_vaast_SubFrame";

    public SubFrame (SubFrameRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public SubFrame (RowKey rowKey) {
        this(new SubFrameRowKey(rowKey.toString()));
    }

    public SubFrameMetadata getMetadata() {
        SubFrameMetadata subFrameMetadata = get(SubFrameMetadata.NAME);
        if (subFrameMetadata == null) {
            addColumnFamily(new SubFrameMetadata());
        }
        return get(SubFrameMetadata.NAME);
    }


}
