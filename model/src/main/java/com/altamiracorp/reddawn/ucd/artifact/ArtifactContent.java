package com.altamiracorp.reddawn.ucd.artifact;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Value;
import org.json.JSONObject;

public class ArtifactContent extends ColumnFamily {
    public static final String NAME = "Content";
    public static final String DOC_ARTIFACT_BYTES = "doc_artifact_bytes";
    public static final String DOC_EXTRACTED_TEXT = "doc_extracted_text";
    public static final String SECURITY = "security";
    public static final String HIGHLIGHTED_TEXT = "highlighted_text";
    public static final String VIDEO_TRANSCRIPT = "video_transcript";

    public ArtifactContent() {
        super(NAME);
    }

    byte[] getDocArtifactBytes() {
        return Value.toBytes(get(DOC_ARTIFACT_BYTES));
    }

    public ArtifactContent setDocArtifactBytes(byte[] docArtifactBytes) {
        set(DOC_ARTIFACT_BYTES, docArtifactBytes);
        return this;
    }

    public byte[] getDocExtractedText() {
        return Value.toBytes(get(DOC_EXTRACTED_TEXT));
    }

    public ArtifactContent setDocExtractedText(byte[] docExtractedText) {
        set(DOC_EXTRACTED_TEXT, docExtractedText);
        return this;
    }

    public String getDocExtractedTextString() {
        byte[] b = getDocExtractedText();
        if (b == null) {
            return null;
        }
        return new String(b);
    }

    public String getSecurity() {
        return Value.toString(get(SECURITY));
    }

    public ArtifactContent setSecurity(String security) {
        set(SECURITY, security);
        return this;
    }

    public String getHighlightedText() {
        return Value.toString(get(HIGHLIGHTED_TEXT));
    }

    public ArtifactContent setHighlightedText(String highlightedText) {
        set(HIGHLIGHTED_TEXT, highlightedText);
        return this;
    }

    public VideoTranscript getVideoTranscript() {
        JSONObject json = Value.toJson(get(VIDEO_TRANSCRIPT));
        if (json == null) {
            return null;
        }
        return new VideoTranscript(json);
    }

    public ArtifactContent setVideoTranscript(VideoTranscript videoTranscript) {
        set(VIDEO_TRANSCRIPT, videoTranscript.toJson().toString());
        return this;
    }
}
