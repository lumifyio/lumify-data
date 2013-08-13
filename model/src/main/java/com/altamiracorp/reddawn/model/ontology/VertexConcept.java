package com.altamiracorp.reddawn.model.ontology;

import com.tinkerpop.blueprints.Vertex;

public class VertexConcept extends Concept {
    private final Vertex vertex;

    public VertexConcept(Vertex vertex) {
        this.vertex = vertex;
    }

    public String getId() {
        return this.vertex.getId().toString();
    }

    public String getTitle() {
        return this.vertex.getProperty("title");
    }
}
