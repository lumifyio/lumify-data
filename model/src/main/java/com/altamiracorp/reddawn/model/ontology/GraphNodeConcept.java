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
        return (String) graphNode.getProperty(PropertyName.ONTOLOGY_TITLE);
    }

    @Override
    public String getGlyphIconResourceRowKey() {
        return (String) graphNode.getProperty(PropertyName.GLYPH_ICON);
    }

    @Override
    public String getColor() {
        return (String) graphNode.getProperty(PropertyName.COLOR);
    }
}
