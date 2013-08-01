package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.graph.GraphNode;
import com.tinkerpop.blueprints.Vertex;

import java.util.Set;

public class TitanGraphNode extends GraphNode {
    private final Vertex vertex;

    public TitanGraphNode(Vertex vertex) {
        this.vertex = vertex;
    }

    @Override
    public String getId() {
        return "" + this.vertex.getId();
    }

    @Override
    public void setProperty(String key, Object value) {
        this.vertex.setProperty(key, value);
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
