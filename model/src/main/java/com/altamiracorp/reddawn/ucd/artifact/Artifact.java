package com.altamiracorp.reddawn.ucd.artifact;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;

import java.util.Date;

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

    public Date getPublishedDate() {
        Date date = getGenericMetadata().getDocumentDtgDate();
        if (date != null) {
            return date;
        }

        date = getGenericMetadata().getFileTimestampDate();
        if (date != null) {
            return date;
        }

        date = getGenericMetadata().getLoadTimestampDate();
        if (date != null) {
            return date;
        }

        return null;
    }

    public ArtifactType getType() {
        // TODO: base this off of content type.
        if (getGenericMetadata().getFileExtension().equals("mp4")) {
            return ArtifactType.VIDEO;
        } else {
            return ArtifactType.DOCUMENT;
        }
    }
}
