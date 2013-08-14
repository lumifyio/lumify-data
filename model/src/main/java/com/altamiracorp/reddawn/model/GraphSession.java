package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.graph.GraphNode;
import com.altamiracorp.reddawn.model.graph.GraphRelationship;
import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.tinkerpop.blueprints.Graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class GraphSession {
    public abstract String save(GraphNode node);

    public abstract String save(GraphRelationship relationship);

    public abstract List<GraphNode> findBy(String key, String value);

    public abstract List<GraphNode> getRelatedNodes(String graphNodeId);

    public abstract List<GraphNode> getResolvedRelatedNodes(String graphNodeId);

    public abstract List<GraphRelationship> getRelationships(List<String> allIds);

    public abstract GraphNode findNode(String graphNodeId);

    public abstract void close();

    public abstract void deleteSearchIndex();

    public abstract HashMap<String, String> getEdgeProperties(String sourceNode, String destNode, String label);

    public abstract Map<String, String> getProperties(String graphNodeId);

    public abstract Map<GraphRelationship, GraphNode> getRelationships(String graphNodeId);

    public abstract List<GraphNode> findByGeoLocation(double latitude, double longitude, double radius);

    public abstract List<GraphNode> searchNodesByTitle(String query);

    public abstract Graph getGraph();

    public abstract List<GraphNode> searchNodesByTitleAndType(String query, VertexType type);

    public abstract GraphNode findNodeByExactTitleAndType(String graphNodeTitle, VertexType type);

    public abstract GraphNode findNodeByOntologyTitleAndType(String title, VertexType concept);

    public abstract void removeRelationship(String source, String target, String label);

    public abstract void commit();
}
