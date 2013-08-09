package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.graph.GraphNode;
import com.altamiracorp.reddawn.model.graph.GraphRelationship;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class GraphSession {
    public static final String PROPERTY_NAME_ROW_KEY = "_rowKey";
    public static final String PROPERTY_NAME_TITLE = "title";
    public static final String PROPERTY_NAME_GEO_LOCATION = "geoLocation";

    public abstract String save(GraphNode node);

    public abstract String save(GraphRelationship relationship);

    public abstract List<GraphNode> findBy(String key, String value);

    public abstract List<GraphNode> getRelatedNodes(String graphNodeId);

    public abstract List<GraphNode> getResolvedRelatedNodes(String graphNodeId);

    public abstract HashMap<String, HashSet<String>> getRelationships(List<String> allIds);

    public abstract GraphNode findNode(String graphNodeId);

    public abstract void close();

    public abstract void deleteSearchIndex();

    public abstract HashMap<String, String> getEdgeProperties(String sourceNode, String destNode);

    public abstract Map<String, String> getProperties(String graphNodeId);

    public abstract Map<GraphRelationship, GraphNode> getRelationships(String graphNodeId);

    public abstract List<GraphNode> findByGeoLocation(double latitude, double longitude, double radius);

    public abstract List<GraphNode> searchNodes(String query);

    public abstract GraphNode findNodeByTitleAndType(String graphNodeTitle, String graphNodeType);
}
