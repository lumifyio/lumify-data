package com.altamiracorp.reddawn.model.ontology;

public enum VertexType {
    CONCEPT("Concept"),
    ARTIFACT("Artifact"),
    ENTITY("Entity"),
    PROPERTY("Property"),
    RELATIONSHIP("Relationship");

    private final String text;

    VertexType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
