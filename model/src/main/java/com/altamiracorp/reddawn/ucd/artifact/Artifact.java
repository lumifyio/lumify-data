package com.altamiracorp.reddawn.ucd.artifact;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;

public class Artifact extends Row<ArtifactRowKey> {
    public static final String TABLE_NAME = "Artifact";

    public Artifact(RowKey rowKey) {
        super(TABLE_NAME, new ArtifactRowKey(rowKey.toString()));
    }

    public Artifact(String rowKey) {
        super(TABLE_NAME, new ArtifactRowKey(rowKey));
    }

    public Artifact() {
        super(TABLE_NAME);
    }

    @Override
    public ArtifactRowKey getRowKey() {
        // TODO should we recalculate this everytime? What if the content isn't loaded?
        ArtifactRowKey rowKey = super.getRowKey();
        if (rowKey == null) {
            rowKey = ArtifactRowKey.build(getContent().getDocArtifactBytes());
        }
        return rowKey;
    }

    public ArtifactContent getContent() {
        ArtifactContent artifactContent = get(ArtifactContent.NAME);
        if (artifactContent == null) {
            addColumnFamily(new ArtifactContent());
        }
        return get(ArtifactContent.NAME);
    }

    public ArtifactGenericMetadata getGenericMetadata() {
        ArtifactGenericMetadata artifactGenericMetadata = get(ArtifactGenericMetadata.NAME);
        if (artifactGenericMetadata == null) {
            addColumnFamily(new ArtifactGenericMetadata());
        }
        return get(ArtifactGenericMetadata.NAME);
    }

    public ArtifactDynamicMetadata getDynamicMetadata() {
        ArtifactDynamicMetadata artifactDynamicMetadata = get(ArtifactDynamicMetadata.NAME);
        if (artifactDynamicMetadata == null) {
            addColumnFamily(new ArtifactDynamicMetadata());
        }
        return get(ArtifactDynamicMetadata.NAME);
    }
}
