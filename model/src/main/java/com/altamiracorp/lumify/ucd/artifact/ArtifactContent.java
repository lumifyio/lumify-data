package com.altamiracorp.lumify.ucd.artifact;

import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Value;
import org.json.JSONException;
import org.json.JSONObject;

public class ArtifactContent extends ColumnFamily {
    public static final String NAME = "Content";
    public static final String DOC_ARTIFACT_BYTES = "doc_artifact_bytes";
    public static final String DOC_EXTRACTED_TEXT = "doc_extracted_text";
    public static final String SECURITY = "security";
    public static final String HIGHLIGHTED_TEXT = "highlighted_text";
    public static final String VIDEO_TRANSCRIPT = "video_transcript";
    public static final String VIDEO_DURATION = "atc:video_duration";

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

    public void mergeVideoTranscript(VideoTranscript videoTranscript) {
        if (getVideoTranscript() != null) {
            setVideoTranscript(getVideoTranscript().merge(videoTranscript));
        } else {
            setVideoTranscript(videoTranscript);
        }
    }

    public ArtifactContent setVideoDuration(long videoDuration) {
        set(VIDEO_DURATION, videoDuration);
        return this;
    }

    public Long getVideoDuration() {
        return Value.toLong(get(VIDEO_DURATION));
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = super.toJson();
            json.put(VIDEO_DURATION, getVideoDuration());
            return json;
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }
}
