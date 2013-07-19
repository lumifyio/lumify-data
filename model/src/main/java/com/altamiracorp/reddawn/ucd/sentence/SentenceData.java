package com.altamiracorp.reddawn.ucd.sentence;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Value;

public class SentenceData extends ColumnFamily {
    public static final String NAME = "Data";
    public static final String ARTIFACT_ID = "artifactId";
    public static final String END = "end";
    public static final String START = "start";
    public static final String TEXT = "text";
    public static final String HIGHLIGHTED_TEXT = "highlighted_text";

    public SentenceData() {
        super(NAME);
    }

    public String getArtifactId() {
        return Value.toString(get(ARTIFACT_ID));
    }

    public SentenceData setArtifactId(String artifactId) {
        set(ARTIFACT_ID, artifactId);
        return this;
    }

    public Long getEnd() {
        return Value.toLong(get(END));
    }

    public SentenceData setEnd(Long end) {
        set(END, end);
        return this;
    }

    public Long getStart() {
        return Value.toLong(get(START));
    }

    public SentenceData setStart(Long start) {
        set(START, start);
        return this;
    }

    public String getText() {
        return Value.toString(get(TEXT));
    }

    public SentenceData setText(String text) {
        set(TEXT, text);
        return this;
    }

    public String getHighlightedText() {
        return Value.toString(get(HIGHLIGHTED_TEXT));
    }

    public SentenceData setHighlightedText(String highlightedText) {
        set(HIGHLIGHTED_TEXT, highlightedText);
        return this;
    }
}
