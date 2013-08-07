package com.altamiracorp.reddawn.model.graph;

import com.altamiracorp.reddawn.model.GraphSession;

import java.util.*;

public class GraphRepository {
    public GraphNode findNode(GraphSession graphSession, String graphNodeId) {
        return graphSession.findNode(graphNodeId);
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

    public Map<String, String> getProperties(GraphSession graphSession, String graphNodeId) {
        return graphSession.getProperties(graphNodeId);
    }

    public Map<GraphRelationship, GraphNode> getRelationships(GraphSession graphSession, String graphNodeId) {
        return graphSession.getRelationships(graphNodeId);
    }

    public HashMap<String, String> getEdgeProperties(GraphSession graphSession, String sourceNode, String destNode) {
        return graphSession.getEdgeProperties(sourceNode, destNode);
    }

    public List<GraphNode> findByGeoLocation(GraphSession graphSession, double latitude, double longitude, double radius) {
        return graphSession.findByGeoLocation(latitude, longitude, radius);
    }
}
