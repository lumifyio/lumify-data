package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.graph.GraphNode;
import com.altamiracorp.reddawn.model.graph.GraphRelationship;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public abstract class GraphSession {
    public abstract String save(GraphNode node);

    public abstract String save(GraphRelationship relationship);

    public abstract List<GraphNode> findBy(String key, String value);

    public abstract List<GraphNode> getRelatedNodes(String graphNodeId);

    public abstract HashMap<String, HashSet<String>> getRelationships(List<String> allIds);
}
