package com.altamiracorp.reddawn.model.ontology;

import com.altamiracorp.reddawn.model.graph.GraphVertex;

public class GraphVertexConcept extends Concept {
    private final GraphVertex graphVertex;

    public GraphVertexConcept(GraphVertex graphVertex) {
        this.graphVertex = graphVertex;
    }

    @Override
    public String getId() {
        return graphVertex.getId();
    }

    @Override
    public String getTitle() {
        return (String) graphVertex.getProperty(OntologyRepository.TITLE_PROPERTY_NAME);
    }

    @Override
    public String getGlyphIconResourceRowKey() {
        return (String) graphVertex.getProperty(OntologyRepository.GLYPH_ICON_PROPERTY_NAME);
    }

    @Override
    public String getColor() {
        return (String) graphVertex.getProperty(OntologyRepository.COLOR_PROPERTY_NAME);
    }
}
