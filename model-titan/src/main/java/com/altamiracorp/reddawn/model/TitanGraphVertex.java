package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.tinkerpop.blueprints.Vertex;

import java.util.Set;

public class TitanGraphVertex extends GraphVertex {
    private final Vertex vertex;

    public TitanGraphVertex(Vertex vertex) {
        this.vertex = vertex;
    }

    @Override
    public String getId() {
        return "" + this.vertex.getId();
    }

    @Override
    public GraphVertex setProperty(String key, Object value) {
        this.vertex.setProperty(key, value);
        return this;
    }

    public Set<String> getPropertyKeys() {
        return this.vertex.getPropertyKeys();
    }

    @Override
    public Object getProperty(String propertyKey) {
        return this.vertex.getProperty(propertyKey);
    }

    public Vertex getVertex() {
        return vertex;
    }
}
