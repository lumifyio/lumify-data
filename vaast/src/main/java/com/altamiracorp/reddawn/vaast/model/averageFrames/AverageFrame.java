package com.altamiracorp.reddawn.vaast.model.averageFrames;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrameMetadata;

public class AverageFrame extends Row<AverageFrameRowKey> {

    public static final String TABLE_NAME = "atc_vaast_AverageFrame";

    public AverageFrame(AverageFrameRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public AverageFrame(RowKey rowKey) {
        this(new AverageFrameRowKey(rowKey.toString()));
    }

    public AverageFrameMetadata getMetadata() {
        AverageFrameMetadata averageFrameMetadata = get(AverageFrameMetadata.NAME);
        if (averageFrameMetadata == null) {
            addColumnFamily(new AverageFrameMetadata());
        }
        return get(AverageFrameMetadata.NAME);
    }

}
