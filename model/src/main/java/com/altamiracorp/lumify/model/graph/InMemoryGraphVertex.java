package com.altamiracorp.lumify.model.graph;

import java.util.HashMap;
import java.util.Set;

public class InMemoryGraphVertex extends GraphVertex {
    private String id;
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    public InMemoryGraphVertex() {
        this.id = null;
    }

    public InMemoryGraphVertex(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public GraphVertex setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public Set<String> getPropertyKeys() {
        return properties.keySet();
    }

    @Override
    public Object getProperty(String propertyKey) {
        return properties.get(propertyKey);
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void update(GraphVertex newGraphVertex) {
        super.update(newGraphVertex);
        if (newGraphVertex.getId() != null) {
            setId(newGraphVertex.getId());
        }
    }
}
