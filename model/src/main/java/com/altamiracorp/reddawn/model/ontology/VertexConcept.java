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
        return getVertex().getProperty("title");
    }

    public Vertex getVertex() {
        return this.vertex;
    }
}
