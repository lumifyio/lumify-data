package com.altamiracorp.lumify.model.artifactThumbnails;

import com.altamiracorp.lumify.model.RowKey;
import org.apache.commons.lang.StringUtils;

public class ArtifactThumbnailRowKey extends RowKey {
    public ArtifactThumbnailRowKey(String rowKey) {
        super(rowKey);
    }

    public ArtifactThumbnailRowKey(String artifactRowKey, int width, int height) {
        super(buildKey(artifactRowKey, width, height));
    }

    private static String buildKey(String artifactRowKey, int width, int height) {
        return artifactRowKey
                + ":"
                + StringUtils.leftPad(Integer.toString(width), 8, '0')
                + StringUtils.leftPad(Integer.toString(height), 8, '0');
    }

    public String getArtifactRowKey() {
        return this.toString().split(":")[0];
    }
}
