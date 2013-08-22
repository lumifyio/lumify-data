package com.altamiracorp.reddawn.model.ontology;

public enum LabelName {
    HAS_PROPERTY("hasProperty"),
    HAS_EDGE("hasEdge"),
    IS_A("isA"),
    HAS_IMAGE("hasImage"),
    HAS_TERM_MENTION("hasTermMention");

    private final String text;

    LabelName(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
