package com.altamiracorp.reddawn.ucd.artifact;

public enum ArtifactType {
    DOCUMENT("document"),
    IMAGE("image"),
    VIDEO("video");

    private final String text;

    ArtifactType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
