package com.altamiracorp.reddawn.model.graph;

import com.altamiracorp.reddawn.model.GraphSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GraphRepository {
    public List<GraphNode> getRelatedNodes(GraphSession graphSession, String graphNodeId) {
        return graphSession.getRelatedNodes(graphNodeId);
    }

    public HashMap<String, HashSet<String>> getRelationships(GraphSession graphSession, List<String> allIds) {
        return graphSession.getRelationships(allIds);
    }
}
