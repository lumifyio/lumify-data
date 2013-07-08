package com.altamiracorp.reddawn.ucd.sentence;

import com.altamiracorp.reddawn.model.RowKey;
import org.apache.commons.lang.StringUtils;

public class SentenceRowKey extends RowKey {
    public SentenceRowKey(String rowKey) {
        super(rowKey);
    }

    public SentenceRowKey(String artifactRowKey, long startOffset, long endOffset) {
        super(buildKey(artifactRowKey, startOffset, endOffset));
    }

    private static String buildKey(String artifactRowKey, long startOffset, long endOffset) {
        return artifactRowKey
                + ":"
                + StringUtils.leftPad(Long.toString(endOffset), 16, '0')
                + ":"
                + StringUtils.leftPad(Long.toString(startOffset), 16, '0');
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
}
