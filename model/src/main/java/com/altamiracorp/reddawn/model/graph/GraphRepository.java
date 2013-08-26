package com.altamiracorp.reddawn.model.graph;

import com.altamiracorp.reddawn.model.GraphSession;
import com.altamiracorp.reddawn.model.ontology.LabelName;
import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphRepository.class.getName());


    public GraphVertex findVertex(GraphSession graphSession, String graphVertexId) {
        return graphSession.findGraphVertex(graphVertexId);
    }

    public List<GraphVertex> findVertices(GraphSession graphSession, String[] vertexIds) {
        return graphSession.findGraphVertices(vertexIds);
    }

    public GraphVertex findVertexByTitleAndType(GraphSession graphSession, String graphVertexTitle, VertexType type) {
        return graphSession.findVertexByExactTitleAndType(graphVertexTitle, type);
    }

    public List<GraphVertex> getRelatedVertices(GraphSession graphSession, String graphVertexId) {
        return graphSession.getRelatedVertices(graphVertexId);
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

    public GraphRelationship saveRelationship(GraphSession graphSession, String sourceGraphVertexId, String destGraphVertexId, String label) {
        GraphRelationship relationship = new GraphRelationship(null, sourceGraphVertexId, destGraphVertexId, label);
        return save(graphSession, relationship);
    }

    public GraphRelationship saveRelationship(GraphSession graphSession, String sourceGraphVertexId, String destGraphVertexId, LabelName label) {
        return saveRelationship(graphSession, sourceGraphVertexId, destGraphVertexId, label.toString());
    }

    public GraphRelationship saveRelationship(GraphSession graphSession, GraphVertex sourceGraphVertex, GraphVertex destGraphVertex, LabelName label) {
        return saveRelationship(graphSession, sourceGraphVertex.getId(), destGraphVertex.getId(), label);
    }

    public String saveVertex(GraphSession graphSession, GraphVertex graphVertex) {
        return graphSession.save(graphVertex);
    }

    public Map<String, String> getProperties(GraphSession graphSession, String graphVertexId) {
        return graphSession.getProperties(graphVertexId);
    }

    public Map<GraphRelationship, GraphVertex> getRelationships(GraphSession graphSession, String graphVertexId) {
        return graphSession.getRelationships(graphVertexId);
    }

    public HashMap<String, String> getEdgeProperties(GraphSession graphSession, String sourceVertex, String destVertex, String label) {
        return graphSession.getEdgeProperties(sourceVertex, destVertex, label);
    }

    public List<GraphVertex> findByGeoLocation(GraphSession graphSession, double latitude, double longitude, double radius) {
        return graphSession.findByGeoLocation(latitude, longitude, radius);
    }

    public List<GraphVertex> searchVerticesByTitle(GraphSession graphSession, String query) {
        return graphSession.searchVerticesByTitle(query);
    }

    public List<GraphVertex> searchVerticesByTitleAndType(GraphSession graphSession, String query, VertexType type) {
        return graphSession.searchVerticesByTitleAndType(query, type);
    }

    public List<GraphVertex> getResolvedRelatedVertices(GraphSession graphSession, String graphVertexId) {
        return graphSession.getResolvedRelatedVertices(graphVertexId);
    }

    public void removeRelationship(GraphSession graphSession, String source, String target, String label) {
        graphSession.removeRelationship(source, target, label);
        return;
    }

    public GraphRelationship findOrAddRelationship(GraphSession graphSession, String sourceVertexId, String targetVertexId, String label) {
        Map<GraphRelationship, GraphVertex> relationships = getRelationships(graphSession, sourceVertexId);
        for (Map.Entry<GraphRelationship, GraphVertex> relationship : relationships.entrySet()) {
            if (relationship.getValue().getId().equals(targetVertexId) &&
                    relationship.getKey().getLabel().equals(label)) {
                return relationship.getKey();
            }
        }
        return this.saveRelationship(graphSession, sourceVertexId, targetVertexId, label);
    }

    public GraphRelationship findOrAddRelationship(GraphSession graphSession, String sourceVertexId, String targetVertexId, LabelName label) {
        return findOrAddRelationship(graphSession, sourceVertexId, targetVertexId, label.toString());
    }

    public GraphRelationship findOrAddRelationship(GraphSession graphSession, GraphVertex sourceVertex, GraphVertex targetVertex, LabelName label) {
        return findOrAddRelationship(graphSession, sourceVertex.getId(), targetVertex.getId(), label);
    }

    public List<List<GraphVertex>> findPath(GraphSession graphSession, GraphVertex sourceVertex, GraphVertex destVertex, int depth) {
        return graphSession.findPath(sourceVertex, destVertex, depth);
    }

    public void remove(GraphSession graphSession, String graphVertexId) {
        graphSession.remove(graphVertexId);
    }

    public void setProperty(GraphSession graphSession, String vertexId, String propertyName, Object value) {
        Vertex vertex = graphSession.getGraph().getVertex(vertexId);

        // TODO: without removing first the property nulls out, this property has to do with Accumulo + Titan
        //       integration in a single row mutation
        vertex.removeProperty(propertyName);
        graphSession.commit();

        vertex.setProperty(propertyName, value);
        LOGGER.info("set property of vertex: " + vertex.getId() + ", property name: " + propertyName + ", value: " + value);
        graphSession.commit();
    }
}
