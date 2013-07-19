package com.altamiracorp.reddawn.ucd.sentence;

import com.altamiracorp.reddawn.model.RowKey;
import org.apache.commons.lang.StringUtils;

public class SentenceRowKey extends RowKey {
    private static final int LEFT_PAD = 16;

    public SentenceRowKey(String rowKey) {
        super(rowKey);
    }

    public SentenceRowKey(String artifactRowKey, long startOffset, long endOffset) {
        super(buildKey(artifactRowKey, startOffset, endOffset));
    }

    private static String buildKey(String artifactRowKey, long startOffset, long endOffset) {
        return artifactRowKey
                + ":"
                + StringUtils.leftPad(Long.toString(endOffset), LEFT_PAD, '0')
                + ":"
                + StringUtils.leftPad(Long.toString(startOffset), LEFT_PAD, '0');
    }

    public String getArtifactRowKey() {
        String[] keyElements = this.toString().split(":");
        int elementsToGet = keyElements.length - 2;
        String result = "";
        for (int i = 0; i < elementsToGet; i++) {
            if (i != 0) {
                result += ":";
            }
            result += keyElements[i];
        }
        return result;
    }

    public Long getStartOffset() {
        String[] keyElements = this.toString().split(":");
        String startOffsetPadded = keyElements[keyElements.length - 1];
        return Long.parseLong(startOffsetPadded);
    }

    public Long getEndOffset() {
        String[] keyElements = this.toString().split(":");
        String startOffsetPadded = keyElements[keyElements.length - 2];
        return Long.parseLong(startOffsetPadded);
    }
}
