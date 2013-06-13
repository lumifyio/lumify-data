package com.altamiracorp.reddawn.ucd.artifact;

import com.altamiracorp.reddawn.model.RowKeyHelper;
import com.altamiracorp.reddawn.model.RowKey;

public class ArtifactRowKey extends RowKey {
    public ArtifactRowKey(String rowKey) {
        super(rowKey);
    }

    public static ArtifactRowKey build(byte[] docArtifactBytes) {
        return new ArtifactRowKey(RowKeyHelper.buildSHA256KeyString(docArtifactBytes));
    }
}
