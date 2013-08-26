package com.altamiracorp.reddawn.model.termMention;

import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import org.apache.commons.lang.StringUtils;

public class TermMentionRowKey extends RowKey {
    public TermMentionRowKey(String rowKey) {
        super(rowKey);
    }

    public TermMentionRowKey(String artifactRowKey, long startOffset, long endOffset) {
        this(buildKey(artifactRowKey, startOffset, endOffset));
    }

    private static String buildKey(String artifactRowKey, long startOffset, long endOffset) {
        return artifactRowKey
                + ":"
                + StringUtils.leftPad(Long.toString(endOffset), RowKeyHelper.OFFSET_WIDTH, '0')
                + ":"
                + StringUtils.leftPad(Long.toString(startOffset), RowKeyHelper.OFFSET_WIDTH, '0');
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

    public long getStartOffset() {
        String[] keyElements = this.toString().split(":");
        String startOffsetPadded = keyElements[keyElements.length - 1];
        return Long.parseLong(startOffsetPadded);
    }

    public long getEndOffset() {
        String[] keyElements = this.toString().split(":");
        String startOffsetPadded = keyElements[keyElements.length - 2];
        return Long.parseLong(startOffsetPadded);
    }
}
