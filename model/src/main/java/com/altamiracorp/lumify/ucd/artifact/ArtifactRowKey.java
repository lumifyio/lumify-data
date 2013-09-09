package com.altamiracorp.lumify.ucd.artifact;

import com.altamiracorp.lumify.model.RowKey;
import com.altamiracorp.lumify.model.RowKeyHelper;

public class ArtifactRowKey extends RowKey {
    public ArtifactRowKey(String rowKey) {
        super(rowKey);
    }

    public static ArtifactRowKey build(byte[] docArtifactBytes) {
        return new ArtifactRowKey(RowKeyHelper.buildSHA256KeyString(docArtifactBytes));
    }
}
