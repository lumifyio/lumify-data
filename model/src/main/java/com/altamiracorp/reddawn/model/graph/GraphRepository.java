package com.altamiracorp.reddawn.model.graph;

import com.altamiracorp.reddawn.model.GraphSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GraphRepository {
    public String getNodeType(GraphSession graphSession, String graphNodeId) {
        return graphSession.getNodeType(graphNodeId);
    }

    public List<GraphNode> getRelatedNodes(GraphSession graphSession, String graphNodeId) {
        return graphSession.getRelatedNodes(graphNodeId);
    }

    public HashMap<String, HashSet<String>> getRelationships(GraphSession graphSession, List<String> allIds) {
        return graphSession.getRelationships(allIds);
    }

    public GraphRelationship saveRelationship(GraphSession graphSession, String sourceGraphNodeId, String destGraphNodeId, String label) {
        GraphRelationship relationship = new GraphRelationship(null, sourceGraphNodeId, destGraphNodeId, label);
        graphSession.save(relationship);
        return relationship;
    }

    public HashMap<String, String> getEdgeProperties (GraphSession graphSession, String sourceNode, String destNode){
        return graphSession.getEdgeProperties (sourceNode, destNode);
    }
}
