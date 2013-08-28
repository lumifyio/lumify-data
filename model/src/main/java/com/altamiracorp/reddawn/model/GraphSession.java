package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.graph.GraphRelationship;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import org.json.JSONArray;

import java.util.List;
import java.util.Map;

public abstract class GraphSession {
    public abstract String save(GraphVertex vertex);

    public abstract String save(GraphRelationship relationship);

    public abstract List<GraphVertex> findBy(String key, String value);

    public abstract List<GraphVertex> getRelatedVertices(String graphVertexId);

    public abstract List<GraphRelationship> getRelationships(List<String> allIds);

    public abstract GraphVertex findGraphVertex(String graphVertexId);

    public abstract List<GraphVertex> findGraphVertices(String[] vertexIds);

    public abstract void close();

    public abstract void deleteSearchIndex();

    public abstract Map<String, String> getEdgeProperties(String sourceVertex, String destVertex, String label);

    public abstract Map<String, String> getVertexProperties(String graphVertexId);

    public abstract Map<GraphRelationship, GraphVertex> getRelationships(String graphVertexId);

    public abstract List<GraphVertex> findByGeoLocation(double latitude, double longitude, double radius);

    public abstract List<GraphVertex> searchVerticesByTitle(String title, JSONArray filterJson);

    public abstract Graph getGraph();

    public abstract List<GraphVertex> searchVerticesByTitleAndType(String query, VertexType type);

    public abstract GraphVertex findVertexByExactTitleAndType(String graphVertexTitle, VertexType graphVertexType);

    public abstract GraphVertex findVertexByOntologyTitleAndType(String title, VertexType concept);

    public abstract GraphVertex findVertexByOntologyTitle(String title);

    public abstract void removeRelationship(String source, String target, String label);

    public abstract void commit();

    public abstract void remove(String graphVertexId);

    public abstract List<List<GraphVertex>> findPath(GraphVertex sourceVertex, GraphVertex destVertex, int depth);

    public abstract GraphVertex findVertexByRowKey(String rowKey);

    public abstract Edge findEdge(String sourceId, String destId, String label);
}
