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
        return getVertex().getProperty(OntologyRepository.TITLE_PROPERTY_NAME);
    }

    @Override
    public String getGlyphIconResourceRowKey() {
        return getVertex().getProperty(OntologyRepository.GLYPH_ICON_PROPERTY_NAME);
    }

    @Override
    public String getColor() {
        return getVertex().getProperty(OntologyRepository.COLOR_PROPERTY_NAME);
    }

    public Vertex getVertex() {
        return this.vertex;
    }
}
