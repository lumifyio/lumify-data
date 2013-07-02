package com.altamiracorp.reddawn.ucd.artifact;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;
import org.json.JSONException;
import org.json.JSONObject;

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

    @Override
    public JSONObject toJson() {
        JSONObject json = super.toJson();
        try {
            json.put("type", getType().toString().toLowerCase());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public ArtifactType getType() {
        if (getGenericMetadata().getMimeType().toLowerCase().contains("video") || getGenericMetadata().getMimeType().toLowerCase().contains("mp4"))
            return ArtifactType.VIDEO;
        else if (getGenericMetadata().getMimeType().toLowerCase().contains("image"))
            return ArtifactType.IMAGE;
        else
            return ArtifactType.DOCUMENT;
    }
}
