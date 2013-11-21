package com.altamiracorp.lumify.core.model;

import com.altamiracorp.lumify.core.model.graph.GraphPagedResults;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.model.graph.GraphRelationship;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.Property;
import com.altamiracorp.lumify.core.model.ontology.PropertyType;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.json.JSONArray;

import java.util.List;
import java.util.Map;

public abstract class GraphSession {
    public abstract String save(GraphVertex vertex, User user);

    public abstract String save(GraphRelationship relationship, User user);

    public abstract List<GraphVertex> getRelatedVertices(String graphVertexId, User user);

    public abstract List<GraphRelationship> getRelationships(List<String> allIds, User user);

    public abstract GraphVertex findGraphVertex(String graphVertexId, User user);

    public abstract List<GraphVertex> findGraphVertices(String[] vertexIds, User user);

    public abstract void close();

    public abstract void deleteSearchIndex(User user);

    public abstract Map<String, String> getEdgeProperties(String sourceVertex, String destVertex, String label, User user);

    public abstract Map<String, String> getVertexProperties(String graphVertexId, User user);

    public abstract Map<GraphRelationship, GraphVertex> getRelationships(String graphVertexId, User user);

    public abstract List<GraphVertex> findByGeoLocation(double latitude, double longitude, double radius, User user);

    public abstract List<GraphVertex> searchVerticesByTitle(String title, JSONArray filterJson);

    public abstract GraphPagedResults searchVerticesByTitle(String title, JSONArray filterJson, User user, long offset, long size, String subType);

    public abstract List<GraphVertex> searchVerticesWithinGraphVertexIds(List<String> artifactGraphVertexIds, JSONArray filterJson, User user);

    public abstract Graph getGraph();

    public abstract List<GraphVertex> searchVerticesByTitleAndType(String query, VertexType type, User user);

    public abstract GraphVertex findVertexByExactTitleAndType(String graphVertexTitle, VertexType graphVertexType, User user);

    public abstract GraphVertex findVertexByOntologyTitleAndType(String title, VertexType concept, User user);

    public abstract GraphVertex findVertexByOntologyTitle(String title, User user);

    public abstract void removeRelationship(String source, String target, String label, User user);

    public abstract void commit();

    public abstract void remove(String graphVertexId, User user);

    public abstract List<List<GraphVertex>> findPath(GraphVertex sourceVertex, GraphVertex destVertex, int dept, int hops, User user);

    public abstract GraphVertex findVertexByRowKey(String rowKey, User user);

    public abstract Edge findEdge(String sourceId, String destId, String label, User user);

    public abstract Property getOrCreatePropertyType(String propertyName, PropertyType dataType, User user);

    public abstract void findOrAddEdge(GraphVertex fromVertex, GraphVertex toVertex, String edgeLabel, User user);

    public abstract GraphVertex getOrCreateRelationshipType(String relationshipName, User user);

    public abstract List<Vertex> getRelationships(Concept sourceConcept, Concept destConcept, User user);

    public abstract Vertex getParentConceptVertex(Vertex vertex, User user);
}
