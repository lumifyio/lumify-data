package com.altamiracorp.lumify.core.model.artifact;

import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import com.altamiracorp.lumify.core.model.ColumnFamily;
import com.altamiracorp.lumify.core.model.Value;

import java.util.Date;

public class ArtifactMetadata extends ColumnFamily {
    public static final String NAME = "Generic_Metadata";
    public static final String RAW = "raw";
    public static final String TEXT = "text";
    public static final String HIGHLIGHTED_TEXT = "highlightedText";
    private static final String GRAPH_VERTEX_ID = "graphVertexId";
    private static final String CREATE_DATE = "createDate";
    private static final String VIDEO_TRANSCRIPT = "videoTranscript";
    private static final String MAPPING_JSON = "mappingJson";

    public ArtifactMetadata() {
        super(NAME);
    }

    public byte[] getRaw() {
        return Value.toBytes(get(RAW));
    }

    public ArtifactMetadata setRaw(byte[] raw) {
        set(RAW, raw);
        return this;
    }

    public String getText() {
        return Value.toString(get(TEXT));
    }

    public ArtifactMetadata setText(String text) {
        set(TEXT, text);
        return this;
    }

    public String getHighlightedText() {
        return Value.toString(get(HIGHLIGHTED_TEXT));
    }

    public ArtifactMetadata setHighlightedText(String highlightedText) {
        set(HIGHLIGHTED_TEXT, highlightedText);
        return this;
    }

    public String getGraphVertexId() {
        return Value.toString(get(GRAPH_VERTEX_ID));
    }

    public ArtifactMetadata setGraphVertexId(String graphVertexId) {
        set(GRAPH_VERTEX_ID, graphVertexId);
        return this;
    }

    public ArtifactMetadata setCreateDate(Date createDate) {
        set(CREATE_DATE, createDate.getTime());
        return this;
    }

    public ArtifactMetadata setVideoTranscript(VideoTranscript videoTranscript) {
        set(VIDEO_TRANSCRIPT, videoTranscript.toJson().toString());
        return this;
    }

    public ArtifactMetadata setMappingJson (String mappingJson) {
        set(MAPPING_JSON, mappingJson);
        return this;
    }
}
