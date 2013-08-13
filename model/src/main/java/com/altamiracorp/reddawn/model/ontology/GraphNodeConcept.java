package com.altamiracorp.reddawn.model.ontology;

import com.altamiracorp.reddawn.model.graph.GraphNode;

public class GraphNodeConcept extends Concept {
    private final GraphNode graphNode;

    public GraphNodeConcept(GraphNode graphNode) {
        this.graphNode = graphNode;
    }

    @Override
    public String getId() {
        return graphNode.getId();
    }

    @Override
    public String getTitle() {
        return (String) graphNode.getProperty(OntologyRepository.TITLE_PROPERTY_NAME);
    }

    @Override
    public String getGlyphIconResourceRowKey() {
        return (String) graphNode.getProperty(OntologyRepository.GLYPH_ICON_PROPERTY_NAME);
    }

    @Override
    public String getColor() {
        return (String) graphNode.getProperty(OntologyRepository.COLOR_PROPERTY_NAME);
    }
}
