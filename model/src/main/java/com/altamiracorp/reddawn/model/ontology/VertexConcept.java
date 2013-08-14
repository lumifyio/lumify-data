package com.altamiracorp.reddawn.model.ontology;

import com.tinkerpop.blueprints.Vertex;

public class VertexConcept extends Concept {
    private final Vertex vertex;

    public VertexConcept(Vertex vertex) {
        this.vertex = vertex;
    }

    public String getId() {
        return getVertex().getId().toString();
    }

    public String getTitle() {
        return getVertex().getProperty(PropertyName.TITLE.toString());
    }

    @Override
    public String getGlyphIconResourceRowKey() {
        return getVertex().getProperty(PropertyName.GLYPH_ICON.toString());
    }

    @Override
    public String getColor() {
        return getVertex().getProperty(PropertyName.COLOR.toString());
    }

    public Vertex getVertex() {
        return this.vertex;
    }
}
