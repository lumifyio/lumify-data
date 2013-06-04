package com.altamiracorp.reddawn.ucd.model;

import com.google.gson.annotations.Expose;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

import java.util.Map;

public class ArtifactContent {
    public static final String COLUMN_FAMILY_NAME = "Content";
    public static final String COLUMN_SECURITY = "security";
    public static final String COLUMN_DOC_ARTIFACT_BYTES = "doc_artifact_bytes";
    public static final String COLUMN_DOC_EXTRACTED_TEXT = "doc_extracted_text";
    public static final String COLUMN_DOC_HIGHLIGHTED_TEXT = "doc_highlighted_text";

    @Expose
    private String security;

    @Expose
    private String docExtractedText;

    @Expose
    private String docHighlightedText;

    private byte[] docArtifactBytes;

    private ArtifactContent() {

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getSecurity() {
        return this.security;
    }

    public String getDocExtractedText() {
        return docExtractedText;
    }

    public String getDocHighlightedText() {
        return docHighlightedText;
    }

    public byte[] getDocArtifactBytes() {
        return docArtifactBytes;
    }

    void addMutations(Mutation mutation) {
        MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_DOC_ARTIFACT_BYTES, getDocArtifactBytes());
        MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_DOC_EXTRACTED_TEXT, getDocExtractedText());
        MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_DOC_HIGHLIGHTED_TEXT, getDocHighlightedText());
        MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_SECURITY, getSecurity());
    }

    public static class Builder {
        private ArtifactContent artifactContent = new ArtifactContent();

        private Builder() {
        }

        public ArtifactContent build() {
            return artifactContent;
        }

        public Builder security(String security) {
            artifactContent.security = security;
            return this;
        }

        public Builder docExtractedText(String docExtractedText) {
            artifactContent.docExtractedText = docExtractedText;
            return this;
        }

        public Builder docHighlightedText(String docHighlightedText) {
            artifactContent.docHighlightedText = docHighlightedText;
            return this;
        }

        public Builder docArtifactBytes(byte[] docArtifactBytes) {
            artifactContent.docArtifactBytes = docArtifactBytes;
            return this;
        }

        static void populateFromColumn(ArtifactContent content, Map.Entry<Key, Value> column) {
            String columnQualifier = column.getKey().getColumnQualifier().toString();
            if (COLUMN_DOC_ARTIFACT_BYTES.equals(columnQualifier)) {
                content.docArtifactBytes = column.getValue().get();
            } else if (COLUMN_DOC_EXTRACTED_TEXT.equals(columnQualifier)) {
                content.docExtractedText = column.getValue().toString();
            } else if (COLUMN_DOC_HIGHLIGHTED_TEXT.equals(columnQualifier)) {
                content.docHighlightedText = column.getValue().toString();
            } else if (COLUMN_SECURITY.equals(columnQualifier)) {
                content.security = column.getValue().toString();
            }
        }
    }
}
