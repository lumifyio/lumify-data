package com.altamiracorp.reddawn.model.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GraphNode {
    private String id;
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    public GraphNode(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public Set<Map.Entry<String, Object>> getProperties() {
        return properties.entrySet();
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
}
