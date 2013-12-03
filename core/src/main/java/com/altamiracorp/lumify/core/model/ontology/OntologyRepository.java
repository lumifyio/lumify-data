package com.altamiracorp.lumify.core.model.ontology;

import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.model.GraphSession;
import com.altamiracorp.lumify.core.model.graph.GraphRelationship;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class OntologyRepository {
    private GraphRepository graphRepository;
    public static final String ROOT_CONCEPT_NAME = "rootConcept";
    private final GraphSession graphSession;

    @Inject
    public OntologyRepository(GraphRepository graphRepository, GraphSession graphSession) {
        this.graphRepository = graphRepository;
        this.graphSession = graphSession;
    }

    public List<Relationship> getRelationshipLabels(User user) {
        Iterable<Vertex> vertices = graphSession.getGraph().query()
                .has(PropertyName.TYPE.toString(), VertexType.RELATIONSHIP.toString())
                .vertices();
        return toRelationships(vertices, user);
    }

    public String getDisplayNameForLabel(String relationshipLabel, User user) {
        Iterable<Vertex> vertices = graphSession.getGraph().query()
                .has(PropertyName.TYPE.toString(), VertexType.RELATIONSHIP.toString())
                .vertices();
        for (Vertex vertex : vertices) {
            if (vertex.getProperty(PropertyName.ONTOLOGY_TITLE.toString()).equals(relationshipLabel))
                return vertex.getProperty(PropertyName.DISPLAY_NAME.toString());
        }
        return null;
    }

    public List<Property> getProperties(User user) {
        List<Property> properties = new ArrayList<Property>();
        Iterator<Vertex> vertices = graphSession.getGraph().query()
                .has(PropertyName.TYPE.toString(), VertexType.PROPERTY.toString())
                .vertices()
                .iterator();
        while (vertices.hasNext()) {
            Vertex vertex = vertices.next();
            properties.add(new VertexProperty(vertex));
        }
        return properties;
    }

    public Property getProperty(String propertyName, User user) {
        Iterator<Vertex> properties = graphSession.getGraph().query()
                .has(PropertyName.TYPE.toString(), VertexType.PROPERTY.toString())
                .has(PropertyName.ONTOLOGY_TITLE.toString(), propertyName)
                .vertices()
                .iterator();
        if (properties.hasNext()) {
            Property property = new VertexProperty(properties.next());
            if (properties.hasNext()) {
                throw new RuntimeException("Too many \"" + propertyName + "\" properties");
            }
            return property;
        } else {
            return null;
        }
    }

    public Concept getRootConcept(User user) {
        Iterator<Vertex> vertices = graphSession.getGraph().query()
                .has(PropertyName.TYPE.toString(), VertexType.CONCEPT.toString())
                .has(PropertyName.ONTOLOGY_TITLE.toString(), OntologyRepository.ROOT_CONCEPT_NAME)
                .vertices()
                .iterator();
        if (vertices.hasNext()) {
            Concept concept = new VertexConcept(vertices.next());
            if (vertices.hasNext()) {
                throw new RuntimeException("Too many \"" + OntologyRepository.ROOT_CONCEPT_NAME + "\" concepts");
            }
            return concept;
        } else {
            throw new RuntimeException("Could not find \"" + OntologyRepository.ROOT_CONCEPT_NAME + "\" concept");
        }
    }

    public List<Concept> getChildConcepts(Concept concept, User user) {
        Vertex conceptVertex = graphSession.getGraph().getVertex(concept.getId());
        return toConcepts(conceptVertex.getVertices(Direction.IN, LabelName.IS_A.toString()));
    }

    public Concept getParentConcept(String conceptId, User user) {
        Vertex conceptVertex = graphSession.getGraph().getVertex(conceptId);
        Vertex parentConceptVertex = graphSession.getParentConceptVertex(conceptVertex, user);
        if (parentConceptVertex == null) {
            return null;
        }
        return new VertexConcept(parentConceptVertex);
    }

    private List<Concept> toConcepts(Iterable<Vertex> vertices) {
        ArrayList<Concept> concepts = new ArrayList<Concept>();
        for (Vertex vertex : vertices) {
            concepts.add(new VertexConcept(vertex));
        }
        return concepts;
    }

    public Concept getConceptById(String conceptVertexId, User user) {
        Vertex conceptVertex = graphSession.getGraph().getVertex(conceptVertexId);
        if (conceptVertex == null) {
            return null;
        }
        return new VertexConcept(conceptVertex);
    }

    public Concept getConceptByName(String title, User user) {
        GraphVertex vertex = graphSession.findVertexByOntologyTitleAndType(title, VertexType.CONCEPT, user);
        if (vertex == null) {
            return null;
        }
        return new GraphVertexConcept(vertex);
    }

    public GraphVertex getGraphVertexByTitleAndType(String title, VertexType type, User user) {
        return graphSession.findVertexByOntologyTitleAndType(title, type, user);
    }

    public GraphVertex getGraphVertexByTitle(String title, User user) {
        return graphSession.findVertexByOntologyTitle(title, user);
    }

    public List<Relationship> getRelationships(String sourceConceptTypeId, String destConceptTypeId, User user) {
        Concept sourceConcept = getConceptById(sourceConceptTypeId, user);
        if (sourceConcept == null) {
            sourceConcept = getConceptByName(sourceConceptTypeId, user);
            if (sourceConcept == null) {
                throw new RuntimeException("Could not find concept: " + sourceConceptTypeId);
            }
        }
        Concept destConcept = getConceptById(destConceptTypeId, user);
        if (destConcept == null) {
            destConcept = getConceptByName(destConceptTypeId, user);
            if (destConcept == null) {
                throw new RuntimeException("Could not find concept: " + destConceptTypeId);
            }
        }

        List<Vertex> relationshipTypes = graphSession.getRelationships(sourceConcept, destConcept, user);
        return toRelationships(relationshipTypes, user);
    }

    private List<Relationship> toRelationships(Iterable<Vertex> relationshipTypes, User user) {
        ArrayList<Relationship> relationships = new ArrayList<Relationship>();
        for (Vertex vertex : relationshipTypes) {
            Concept[] relatedConcepts = getRelationshipRelatedConcepts("" + vertex.getId(), (String) vertex.getProperty(PropertyName.ONTOLOGY_TITLE.toString()), user);
            relationships.add(new VertexRelationship(vertex, relatedConcepts[0], relatedConcepts[1]));
        }
        return relationships;
    }

    private Concept[] getRelationshipRelatedConcepts(String vertexId, String ontologyTitle, User user) {
        Concept[] sourceAndDestConcept = new Concept[2];
        Map<GraphRelationship, GraphVertex> related = graphSession.getRelationships(vertexId, user);
        for (Map.Entry<GraphRelationship, GraphVertex> relatedVertex : related.entrySet()) {
            String type = (String) relatedVertex.getValue().getProperty(PropertyName.TYPE);
            if (type.equals(VertexType.CONCEPT.toString())) {
                String destVertexId = relatedVertex.getKey().getDestVertexId();
                String sourceVertexId = relatedVertex.getKey().getSourceVertexId();
                if (sourceVertexId.equals(vertexId)) {
                    if (sourceAndDestConcept[0] != null) {
                        throw new RuntimeException("Invalid relationship '" + ontologyTitle + "'. Wrong number of related concepts.");
                    }
                    sourceAndDestConcept[0] = new GraphVertexConcept(relatedVertex.getValue());
                } else if (destVertexId.equals(vertexId)) {
                    if (sourceAndDestConcept[1] != null) {
                        throw new RuntimeException("Invalid relationship '" + ontologyTitle + "'. Wrong number of related concepts.");
                    }
                    sourceAndDestConcept[1] = new GraphVertexConcept(relatedVertex.getValue());
                }
            }
        }
        if (sourceAndDestConcept[0] == null || sourceAndDestConcept[1] == null) {
            throw new RuntimeException("Invalid relationship '" + ontologyTitle + "'. Wrong number of related concepts.");
        }
        return sourceAndDestConcept;
    }

    public List<Property> getPropertiesByConceptId(String conceptVertexId, User user) {
        Concept conceptVertex = getConceptById(conceptVertexId, user);
        if (conceptVertex == null) {
            conceptVertex = getConceptByName(conceptVertexId, user);
            if (conceptVertex == null) {
                throw new RuntimeException("Could not find concept: " + conceptVertexId);
            }
        }
        return getPropertiesByVertex(conceptVertex.getVertex(), user);
    }

    private List<Property> getPropertiesByVertex(Vertex vertex, User user) {
        List<Property> properties = new ArrayList<Property>();

        Iterator<Vertex> propertyVertices = vertex.getVertices(Direction.OUT, LabelName.HAS_PROPERTY.toString()).iterator();
        while (propertyVertices.hasNext()) {
            Vertex propertyVertex = propertyVertices.next();
            properties.add(new VertexProperty(propertyVertex));
        }

        Vertex parentConceptVertex = graphSession.getParentConceptVertex(vertex, user);
        if (parentConceptVertex != null) {
            List<Property> parentProperties = getPropertiesByVertex(parentConceptVertex, user);
            properties.addAll(parentProperties);
        }

        return properties;
    }

    public List<Property> getPropertiesByConceptIdNoRecursion(String conceptVertexId, User user) {
        Vertex conceptVertex = graphSession.getGraph().getVertex(conceptVertexId);
        if (conceptVertex == null) {
            throw new RuntimeException("Could not find concept: " + conceptVertexId);
        }
        return getPropertiesByVertexNoRecursion(conceptVertex);
    }

    private List<Property> getPropertiesByVertexNoRecursion(Vertex vertex) {
        List<Property> properties = new ArrayList<Property>();

        Iterator<Vertex> propertyVertices = vertex.getVertices(Direction.OUT, LabelName.HAS_PROPERTY.toString()).iterator();
        while (propertyVertices.hasNext()) {
            Vertex propertyVertex = propertyVertices.next();
            properties.add(new VertexProperty(propertyVertex));
        }

        return properties;
    }

    public Property getPropertyById(String propertyId, User user) {
        List<Property> properties = getProperties(user);
        for (Property property : properties) {
            if (property.getId().equals(propertyId)) {
                return property;
            }
        }
        return null;
    }

    public List<Concept> getConceptByIdAndChildren(String conceptId, User user) {
        ArrayList<Concept> concepts = new ArrayList<Concept>();
        Concept concept = getConceptById(conceptId, user);
        if (concept == null) {
            return null;
        }
        concepts.add(concept);
        List<Concept> children = getChildConcepts(concept, user);
        concepts.addAll(children);
        return concepts;
    }

    public List<Property> getPropertiesByRelationship(String relationshipLabel, User user) {
        Vertex relationshipVertex = getRelationshipVertexId(relationshipLabel, user);
        if (relationshipVertex == null) {
            throw new RuntimeException("Could not find relationship: " + relationshipLabel);
        }
        return getPropertiesByVertex(relationshipVertex, user);
    }

    private Vertex getRelationshipVertexId(String relationshipLabel, User user) {
        Iterator<Vertex> vertices = graphSession.getGraph().query()
                .has(PropertyName.TYPE.toString(), VertexType.RELATIONSHIP.toString())
                .has(PropertyName.ONTOLOGY_TITLE.toString(), relationshipLabel)
                .vertices()
                .iterator();
        if (vertices.hasNext()) {
            Vertex vertex = vertices.next();
            if (vertices.hasNext()) {
                throw new RuntimeException("Too many \"" + VertexType.RELATIONSHIP + "\" vertices");
            }
            return vertex;
        } else {
            throw new RuntimeException("Could not find \"" + VertexType.RELATIONSHIP + "\" vertex");
        }
    }

    public Concept getOrCreateConcept(Concept parent, String conceptName, String displayName, User user) {
        Concept concept = getConceptByName(conceptName, user);
        if (concept == null) {
            InMemoryGraphVertex graphVertex = new InMemoryGraphVertex();
            String id = graphRepository.saveVertex(graphVertex, user);
            concept = getConceptById(id, user);
        }
        concept.setProperty(PropertyName.TYPE.toString(), VertexType.CONCEPT.toString());
        concept.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), conceptName);
        concept.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
        if (parent != null) {
            graphRepository.findOrAddRelationship(concept, parent, LabelName.IS_A, user);
        }
        return concept;
    }

    protected void findOrAddEdge(GraphVertex fromVertex, GraphVertex toVertex, String edgeLabel, User user) {
        graphSession.findOrAddEdge(fromVertex, toVertex, edgeLabel, user);
    }

    public Property addPropertyTo(GraphVertex vertex, String propertyName, String displayName, PropertyType dataType, User user) {
        checkNotNull(vertex, "vertex was null");
        Property property = graphSession.getOrCreatePropertyType(propertyName, dataType, user);
        property.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
        graphSession.commit();

        findOrAddEdge(vertex, property, LabelName.HAS_PROPERTY.toString(), user);
        graphSession.commit();

        return property;
    }

    public Property addPropertyTo(String relationshipLabel, String propertyName, String displayName, PropertyType dataType, User user) {
        Relationship vertex = getRelationshipByName(relationshipLabel, user);
        return addPropertyTo(vertex, propertyName, displayName, dataType, user);
    }

    public Relationship getRelationshipByName(String title, User user) {
        GraphVertex vertex = graphSession.findVertexByOntologyTitleAndType(title, VertexType.RELATIONSHIP, user);
        if (vertex == null) {
            return null;
        }
        Concept[] relatedConcepts = getRelationshipRelatedConcepts(vertex.getId(), (String) vertex.getProperty(PropertyName.ONTOLOGY_TITLE), user);
        return new GraphVertexRelationship(vertex, relatedConcepts[0], relatedConcepts[1]);
    }

    protected Relationship getOrCreateRelationship(String relationshipLabel, String displayName, User user) {
        Relationship relationship = getRelationshipByName(relationshipLabel, user);
        if (relationship == null) {
            InMemoryGraphVertex graphVertex = new InMemoryGraphVertex();
            graphVertex.setProperty(PropertyName.TYPE.toString(), VertexType.RELATIONSHIP.toString());
            graphVertex.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), relationshipLabel);
            graphRepository.saveVertex(graphVertex, user);
            graphSession.commit();
            relationship = getRelationshipByName(relationshipLabel, user);
        }
        relationship.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
        return relationship;
    }

    public GraphVertex getOrCreateRelationshipType(GraphVertex fromVertex, GraphVertex toVertex, String relationshipName, String displayName, User user) {
        GraphVertex relationshipLabel = graphSession.getOrCreateRelationshipType(relationshipName, user);
        relationshipLabel.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
        graphSession.commit();

        findOrAddEdge(fromVertex, relationshipLabel, LabelName.HAS_EDGE.toString(), user);
        findOrAddEdge(relationshipLabel, toVertex, LabelName.HAS_EDGE.toString(), user);
        graphSession.commit();

        return relationshipLabel;
    }

    public void resolvePropertyIds(JSONArray filterJson, User user) throws JSONException {
        for (int i = 0; i < filterJson.length(); i++) {
            JSONObject filter = filterJson.getJSONObject(i);
            if (filter.has("propertyId") && !filter.has("propertyName")) {
                String propertyId = filter.getString("propertyId");
                Property property = getPropertyById(propertyId, user);
                if (property == null) {
                    throw new RuntimeException("Could not find property with id: " + propertyId);
                }
                filter.put("propertyName", property.getTitle());
                filter.put("propertyDataType", property.getDataType());
            }
        }
    }
}
