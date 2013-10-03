package com.altamiracorp.lumify.ucd.artifact;

import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Value;

import java.util.Date;

public class ArtifactMetadata extends ColumnFamily {
    public static final String NAME = "Generic_Metadata";
    public static final String RAW = "raw";
    public static final String TEXT = "text";
    public static final String HIGHLIGHTED_TEXT = "highlightedText";
    private static final String GRAPH_VERTEX_ID = "graphVertexId";
    private static final String CREATE_DATE = "createDate";

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
}
