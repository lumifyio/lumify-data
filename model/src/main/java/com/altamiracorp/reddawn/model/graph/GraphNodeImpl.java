package com.altamiracorp.reddawn.model.graph;

import java.util.HashMap;
import java.util.Set;

public class GraphNodeImpl implements GraphNode {
    private String id;
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    public GraphNodeImpl(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
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
