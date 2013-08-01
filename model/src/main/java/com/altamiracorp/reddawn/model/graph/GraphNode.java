package com.altamiracorp.reddawn.model.graph;

import java.util.Set;

public interface GraphNode {
    String getId();

    void setProperty(String key, Object value);

    Set<String> getPropertyKeys();

    Object getProperty(String propertyKey);
}
