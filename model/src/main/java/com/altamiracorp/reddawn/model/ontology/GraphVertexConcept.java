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
        return (String) graphVertex.getProperty(PropertyName.TITLE);
    }

    @Override
    public String getGlyphIconResourceRowKey() {
        return (String) graphVertex.getProperty(PropertyName.GLYPH_ICON);
    }

    @Override
    public String getColor() {
        return (String) graphVertex.getProperty(PropertyName.COLOR);
    }

    @Override
    public String getDisplayName() {
        return (String) graphVertex.getProperty(PropertyName.DISPLAY_NAME);
    }

    public GraphVertex getGraphVertex() {
        return graphVertex;
    }
}
