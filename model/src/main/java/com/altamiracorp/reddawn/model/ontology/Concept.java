package com.altamiracorp.reddawn.model.ontology;

import com.tinkerpop.blueprints.Vertex;

public class Concept {
    private final Vertex vertex;

    public Concept(Vertex vertex) {
        this.vertex = vertex;
    }

    public Object getId() {
        return this.vertex.getId();
    }

    public String getTitle() {
        return this.vertex.getProperty("title");
    }
}
