package com.altamiracorp.reddawn.model.graph;

import com.altamiracorp.reddawn.model.GraphSession;
import com.altamiracorp.reddawn.model.ontology.VertexType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class GraphRepository {
    public GraphNode findNode(GraphSession graphSession, String graphNodeId) {
        return graphSession.findNode(graphNodeId);
    }

    public GraphNode findNodeByTitleAndType(GraphSession graphSession, String graphNodeTitle, VertexType type) {
        return graphSession.findNodeByExactTitleAndType(graphNodeTitle, type);
    }

    public List<GraphNode> getRelatedNodes(GraphSession graphSession, String graphNodeId) {
        return graphSession.getRelatedNodes(graphNodeId);
    }

    public List<GraphRelationship> getRelationships(GraphSession graphSession, List<String> allIds) {
        return graphSession.getRelationships(allIds);
    }

    public void saveMany(GraphSession graphSession, List<GraphRelationship> graphRelationships) {
        for (GraphRelationship graphRelationship : graphRelationships) {
            save(graphSession, graphRelationship);
        }
    }

    private GraphRelationship save(GraphSession graphSession, GraphRelationship graphRelationship) {
        graphSession.save(graphRelationship);
        return graphRelationship;
    }

    public GraphRelationship saveRelationship(GraphSession graphSession, String sourceGraphNodeId, String destGraphNodeId, String label) {
        GraphRelationship relationship = new GraphRelationship(null, sourceGraphNodeId, destGraphNodeId, label);
        return save(graphSession, relationship);
    }

    public String saveNode(GraphSession graphSession, GraphNode graphNode) {
        return graphSession.save(graphNode);
    }

    public Map<String, String> getProperties(GraphSession graphSession, String graphNodeId) {
        return graphSession.getProperties(graphNodeId);
    }

    public Map<GraphRelationship, GraphNode> getRelationships(GraphSession graphSession, String graphNodeId) {
        return graphSession.getRelationships(graphNodeId);
    }

    public HashMap<String, String> getEdgeProperties(GraphSession graphSession, String sourceNode, String destNode, String label) {
        return graphSession.getEdgeProperties(sourceNode, destNode, label);
    }

    public List<GraphNode> findByGeoLocation(GraphSession graphSession, double latitude, double longitude, double radius) {
        return graphSession.findByGeoLocation(latitude, longitude, radius);
    }

    public List<GraphNode> searchNodesByTitle(GraphSession graphSession, String query) {
        return graphSession.searchNodesByTitle(query);
    }

    public List<GraphNode> searchNodesByTitleAndType(GraphSession graphSession, String query, VertexType type) {
        return graphSession.searchNodesByTitleAndType(query, type);
    }

    public List<GraphNode> getResolvedRelatedNodes(GraphSession graphSession, String graphNodeId) {
        return graphSession.getResolvedRelatedNodes(graphNodeId);
    }

    public void removeRelationship(GraphSession graphSession, String source, String target, String label) {
        graphSession.removeRelationship(source, target, label);
        return;
    }
}
