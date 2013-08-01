package com.altamiracorp.reddawn.model.graph;

import java.util.HashMap;
import java.util.Set;

public class GraphRelationship {
    private final String id;
    private final String sourceNodeId;
    private final String destNodeId;
    private String label;
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    public GraphRelationship(String id, String sourceNodeId, String destNodeId, String label) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.destNodeId = destNodeId;
        this.label = label;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public String getDestNodeId() {
        return destNodeId;
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
