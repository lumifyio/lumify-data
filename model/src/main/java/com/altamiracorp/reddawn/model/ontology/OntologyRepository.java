package com.altamiracorp.reddawn.model.ontology;

import com.altamiracorp.reddawn.model.GraphSession;
import com.altamiracorp.reddawn.model.graph.GraphRelationship;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.graph.InMemoryGraphVertex;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OntologyRepository {
    private GraphRepository graphRepository = new GraphRepository();
    public static final String ROOT_CONCEPT_NAME = "rootConcept";

    public List<Relationship> getRelationshipLabels(GraphSession graphSession) {
        Iterable<Vertex> vertices = graphSession.getGraph().query()
                .has(PropertyName.TYPE.toString(), VertexType.RELATIONSHIP.toString())
                .vertices();
        return toRelationships(graphSession, vertices);
    }

    public List<Property> getProperties(GraphSession graphSession) {
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

    public Property getProperty(GraphSession graphSession, String propertyName) {
        Iterator<Vertex> properties = graphSession.getGraph().query()
                .has(PropertyName.TYPE.toString(), VertexType.PROPERTY.toString())
                .has(PropertyName.ONTOLOGY_TITLE.toString(), propertyName)
                .vertices()
                .iterator();
        if (properties.hasNext()) {
            Property property = new VertexProperty(properties.next());
            if (properties.hasNext()) {
                throw new RuntimeException("Too many \"" + VertexType.ENTITY + "\" properties");
            }
            return property;
        } else {
            throw new RuntimeException("Could not find \"" + VertexType.ENTITY + "\" property");
        }
    }

    public Concept getRootConcept(GraphSession graphSession) {
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

    public List<Concept> getChildConcepts(GraphSession graphSession, Concept concept) {
        Vertex conceptVertex = graphSession.getGraph().getVertex(concept.getId());
        return toConcepts(conceptVertex.getVertices(Direction.IN, LabelName.IS_A.toString()));
    }

    private List<Concept> toConcepts(Iterable<Vertex> vertices) {
        ArrayList<Concept> concepts = new ArrayList<Concept>();
        for (Vertex vertex : vertices) {
            concepts.add(new VertexConcept(vertex));
        }
        return concepts;
    }

    public Concept getConceptById(GraphSession graphSession, String conceptVertexId) {
        Vertex conceptVertex = graphSession.getGraph().getVertex(conceptVertexId);
        if (conceptVertex == null) {
            return null;
        }
        return new VertexConcept(conceptVertex);
    }

    private Vertex getParentConceptVertex(Vertex conceptVertex) {
        Iterator<Vertex> parents = conceptVertex.getVertices(Direction.OUT, LabelName.IS_A.toString()).iterator();
        if (!parents.hasNext()) {
            return null;
        }
        Vertex v = parents.next();
        if (parents.hasNext()) {
            throw new RuntimeException("Unexpected number of parents for concept: " + conceptVertex.getProperty(PropertyName.TITLE.toString()));
        }
        return v;
    }

    public Concept getConceptByName(GraphSession graphSession, String title) {
        GraphVertex vertex = graphSession.findVertexByOntologyTitleAndType(title, VertexType.CONCEPT);
        if (vertex == null) {
            return null;
        }
        return new GraphVertexConcept(vertex);
    }

    public GraphVertex getGraphVertexByTitleAndType(GraphSession graphSession, String title, VertexType type) {
        return graphSession.findVertexByOntologyTitleAndType(title, type);
    }

    public GraphVertex getGraphVertexByTitle(GraphSession graphSession, String title) {
        return graphSession.findVertexByOntologyTitle(title);
    }

    public List<Relationship> getRelationships(GraphSession graphSession, String sourceConceptTypeId, String destConceptTypeId) {
        VertexConcept sourceConcept = (VertexConcept) getConceptById(graphSession, sourceConceptTypeId);
        if (sourceConcept == null) {
            throw new RuntimeException("Could not find concept: " + sourceConceptTypeId);
        }
        final VertexConcept destConcept = (VertexConcept) getConceptById(graphSession, destConceptTypeId);
        if (destConcept == null) {
            throw new RuntimeException("Could not find concept: " + destConceptTypeId);
        }

        List<Vertex> relationshipTypes = new GremlinPipeline(sourceConcept.getVertex())
                .outE(LabelName.HAS_EDGE.toString())
                .inV()
                .as("edgeTypes")
                .outE(LabelName.HAS_EDGE.toString())
                .inV()
                .filter(new PipeFunction<Vertex, Boolean>() {
                    @Override
                    public Boolean compute(Vertex vertex) {
                        return vertex.getId().toString().equals(destConcept.getId());
                    }
                })
                .back("edgeTypes")
                .toList();
        return toRelationships(graphSession, relationshipTypes);
    }

    private List<Relationship> toRelationships(GraphSession graphSession, Iterable<Vertex> relationshipTypes) {
        ArrayList<Relationship> relationships = new ArrayList<Relationship>();
        for (Vertex vertex : relationshipTypes) {
            Concept[] relatedConcepts = getRelationshipRelatedConcepts(graphSession, "" + vertex.getId(), (String) vertex.getProperty(PropertyName.ONTOLOGY_TITLE.toString()));
            relationships.add(new VertexRelationship(vertex, relatedConcepts[0], relatedConcepts[1]));
        }
        return relationships;
    }

    private Concept[] getRelationshipRelatedConcepts(GraphSession graphSession, String vertexId, String ontologyTitle) {
        Concept[] sourceAndDestConcept = new Concept[2];
        Map<GraphRelationship, GraphVertex> related = graphSession.getRelationships(vertexId);
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

    public List<Property> getPropertiesByConceptId(GraphSession graphSession, String conceptVertexId) {
        Vertex conceptVertex = graphSession.getGraph().getVertex(conceptVertexId);
        if (conceptVertex == null) {
            throw new RuntimeException("Could not find concept: " + conceptVertexId);
        }
        return getPropertiesByVertex(graphSession, conceptVertex);
    }

    private List<Property> getPropertiesByVertex(GraphSession graphSession, Vertex vertex) {
        List<Property> properties = new ArrayList<Property>();

        Iterator<Vertex> propertyVertices = vertex.getVertices(Direction.OUT, LabelName.HAS_PROPERTY.toString()).iterator();
        while (propertyVertices.hasNext()) {
            Vertex propertyVertex = propertyVertices.next();
            properties.add(new VertexProperty(propertyVertex));
        }

        Vertex parentConceptVertex = getParentConceptVertex(vertex);
        if (parentConceptVertex != null) {
            List<Property> parentProperties = getPropertiesByVertex(graphSession, parentConceptVertex);
            properties.addAll(parentProperties);
        }

        return properties;
    }

    public List<Property> getPropertiesByConceptIdNoRecursion(GraphSession graphSession, String conceptVertexId) {
        Vertex conceptVertex = graphSession.getGraph().getVertex(conceptVertexId);
        if (conceptVertex == null) {
            throw new RuntimeException("Could not find concept: " + conceptVertexId);
        }
        return getPropertiesByVertexNoRecursion(graphSession, conceptVertex);
    }

    private List<Property> getPropertiesByVertexNoRecursion(GraphSession graphSession, Vertex vertex) {
        List<Property> properties = new ArrayList<Property>();

        Iterator<Vertex> propertyVertices = vertex.getVertices(Direction.OUT, LabelName.HAS_PROPERTY.toString()).iterator();
        while (propertyVertices.hasNext()) {
            Vertex propertyVertex = propertyVertices.next();
            properties.add(new VertexProperty(propertyVertex));
        }

        return properties;
    }

    public Property getPropertyById(GraphSession graphSession, int propertyId) {
        List<Property> properties = getProperties(graphSession);
        for (Property property : properties) {
            if (property.getId().equals("" + propertyId)) {
                return property;
            }
        }
        return null;
    }

    public List<Concept> getConceptByIdAndChildren(GraphSession graphSession, String conceptId) {
        ArrayList<Concept> concepts = new ArrayList<Concept>();
        Concept concept = getConceptById(graphSession, conceptId);
        if (concept == null) {
            return null;
        }
        concepts.add(concept);
        List<Concept> children = getChildConcepts(graphSession, concept);
        concepts.addAll(children);
        return concepts;
    }

    public List<Property> getPropertiesByRelationship(GraphSession graphSession, String relationshipLabel) {
        Vertex relationshipVertex = getRelationshipVertexId(graphSession, relationshipLabel);
        if (relationshipVertex == null) {
            throw new RuntimeException("Could not find relationship: " + relationshipLabel);
        }
        return getPropertiesByVertex(graphSession, relationshipVertex);
    }

    private Vertex getRelationshipVertexId(GraphSession graphSession, String relationshipLabel) {
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

    public Concept getOrCreateConcept(GraphSession graphSession, Concept parent, String conceptName, String displayName) {
        Concept concept = getConceptByName(graphSession, conceptName);
        if (concept == null) {
            InMemoryGraphVertex graphVertex = new InMemoryGraphVertex();
            String id = graphRepository.saveVertex(graphSession, graphVertex);
            concept = getConceptById(graphSession, id);
        }
        concept.setProperty(PropertyName.TYPE.toString(), VertexType.CONCEPT.toString());
        concept.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), conceptName);
        concept.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
        if (parent != null) {
            graphRepository.findOrAddRelationship(graphSession, concept, parent, LabelName.IS_A);
        }
        return concept;
    }

    protected void findOrAddEdge(GraphSession graphSession, GraphVertex fromVertex, GraphVertex toVertex, String edgeLabel) {
        graphSession.findOrAddEdge(fromVertex, toVertex, edgeLabel);
    }

    public Property addPropertyTo(GraphSession graphSession, GraphVertex vertex, String propertyName, String displayName, PropertyType dataType) {
        Property property = graphSession.getOrCreatePropertyType(propertyName, dataType);
        property.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
        graphSession.commit();

        findOrAddEdge(graphSession, vertex, property, LabelName.HAS_PROPERTY.toString());
        graphSession.commit();

        return property;
    }

    public Property addPropertyTo(GraphSession graphSession, String relationshipLabel, String propertyName, String displayName, PropertyType dataType) {
        Relationship vertex = getRelationshipByName(graphSession, relationshipLabel);
        return addPropertyTo(graphSession, vertex, propertyName, displayName, dataType);
    }

    public Relationship getRelationshipByName(GraphSession graphSession, String title) {
        GraphVertex vertex = graphSession.findVertexByOntologyTitleAndType(title, VertexType.RELATIONSHIP);
        if (vertex == null) {
            return null;
        }
        Concept[] relatedConcepts = getRelationshipRelatedConcepts(graphSession, vertex.getId(), (String) vertex.getProperty(PropertyName.ONTOLOGY_TITLE));
        return new GraphVertexRelationship(vertex, relatedConcepts[0], relatedConcepts[1]);
    }

    protected Relationship getOrCreateRelationship(GraphSession graphSession, String relationshipLabel, String displayName) {
        Relationship relationship = getRelationshipByName(graphSession, relationshipLabel);
        if (relationship == null) {
            InMemoryGraphVertex graphVertex = new InMemoryGraphVertex();
            graphVertex.setProperty(PropertyName.TYPE.toString(), VertexType.RELATIONSHIP.toString());
            graphVertex.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), relationshipLabel);
            graphRepository.saveVertex(graphSession, graphVertex);
            graphSession.commit();
            relationship = getRelationshipByName(graphSession, relationshipLabel);
        }
        relationship.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
        return relationship;
    }

    public GraphVertex getOrCreateRelationshipType(GraphSession graphSession, GraphVertex fromVertex, GraphVertex toVertex, String relationshipName, String displayName) {
        GraphVertex relationshipLabel = graphSession.getOrCreateRelationshipType(relationshipName);
        relationshipLabel.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
        graphSession.commit();

        findOrAddEdge(graphSession, fromVertex, relationshipLabel, LabelName.HAS_EDGE.toString());
        findOrAddEdge(graphSession, relationshipLabel, toVertex, LabelName.HAS_EDGE.toString());
        graphSession.commit();

        return relationshipLabel;
    }
}
