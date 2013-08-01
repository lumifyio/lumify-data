package com.altamiracorp.reddawn.model.graph;

import java.util.HashMap;
import java.util.Set;

public class GraphRelationship {
    private final String id;
    private final GraphNode source;
    private final GraphNode dest;
    private String label;
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    public GraphRelationship(String id, GraphNode source, GraphNode dest, String label) {
        this.id = id;
        this.source = source;
        this.dest = dest;
        this.label = label;
    }

    public GraphNode getSource() {
        return source;
    }

    public GraphNode getDest() {
        return dest;
    }

    public String getId() {
        return this.id;
    }

    public String getLabel() {
        return label;
    }

    public Set<String> getPropertyKeys() {
        return properties.keySet();
    }

    public Object getProperty(String propertyKey) {
        return properties.get(propertyKey);
    }
}
