package com.altamiracorp.reddawn.vaast.model.subFrames;

import com.altamiracorp.reddawn.model.RowKey;
import org.apache.commons.lang.StringUtils;

public class SubFrameRowKey extends RowKey {

    public SubFrameRowKey (String subFrameRowKey) {
        super(subFrameRowKey);
    }

    public SubFrameRowKey (String frameRowKey, int subFrameNumber) {
        super(buildKey(frameRowKey, subFrameNumber));
    }

    private static String buildKey(String frameRowKey, long subFrameNumber) {
        return frameRowKey
                + ":"
                + StringUtils.leftPad(Long.toString(subFrameNumber), 16, '0');
    }

    public String getArtifactRowKey () {
        return this.toString().split(":")[0];
    }

}
