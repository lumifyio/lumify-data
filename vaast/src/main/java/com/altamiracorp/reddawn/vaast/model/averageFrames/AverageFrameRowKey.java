package com.altamiracorp.reddawn.vaast.model.averageFrames;

import com.altamiracorp.reddawn.model.RowKey;
import org.apache.commons.lang.StringUtils;

public class AverageFrameRowKey extends RowKey {

    public AverageFrameRowKey (String averageFrameRowKey) {
        super(averageFrameRowKey);
    }

    public AverageFrameRowKey(String artifactRowKey, long frameGroup) {
        super(buildKey(artifactRowKey, frameGroup));
    }

    private static String buildKey(String artifactRowKey, long frameGroup) {
        return artifactRowKey
                + ":"
                + StringUtils.leftPad(Long.toString(frameGroup), 16, '0');
    }

    public Long getFrameGroup() {
        return Long.parseLong(this.toString().split(":")[1]);
    }


}
