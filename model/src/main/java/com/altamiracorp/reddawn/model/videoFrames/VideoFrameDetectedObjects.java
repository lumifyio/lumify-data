package com.altamiracorp.reddawn.model.videoFrames;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.RowKeyHelper;

public class VideoFrameDetectedObjects extends ColumnFamily {

    public static final String NAME = "detected_objects";

    public VideoFrameDetectedObjects() {
        super(NAME);
    }

    public void addDetectedObject(String concept, String model, String[] coords) {
        String coordKey = RowKeyHelper.buildMinor(coords);
        String columnName = RowKeyHelper.buildMinor(concept,model,coordKey);
        this.set(columnName,"");
    }
}
