package com.altamiracorp.reddawn.model.graph;

import java.util.HashMap;
import java.util.Set;

public class GraphNodeImpl extends GraphNode {
    private String id;
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    public GraphNodeImpl() {
        this.id = null;
    }

    public GraphNodeImpl(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public GraphNode setProperty(String key, Object value) {
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
}
