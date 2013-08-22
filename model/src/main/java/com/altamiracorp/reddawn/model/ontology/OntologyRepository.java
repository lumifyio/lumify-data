package com.altamiracorp.reddawn.model.ontology;

import com.altamiracorp.reddawn.model.GraphSession;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OntologyRepository {
    public List<Relationship> getRelationshipLabels(GraphSession graphSession) {
        List<Relationship> relationships = new ArrayList<Relationship>();
        Iterator<Vertex> vertices = graphSession.getGraph().query()
                .has(PropertyName.TYPE.toString(), VertexType.RELATIONSHIP.toString())
                .vertices()
                .iterator();
        while (vertices.hasNext()) {
            Vertex vertex = vertices.next();
            relationships.add(new VertexRelationship(vertex));
        }
        return relationships;
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

    public Concept getEntityConcept(GraphSession graphSession) {
        Iterator<Vertex> vertices = graphSession.getGraph().query()
                .has(PropertyName.TYPE.toString(), VertexType.CONCEPT.toString())
                .has(PropertyName.ONTOLOGY_TITLE.toString(), VertexType.ENTITY.toString())
                .vertices()
                .iterator();
        if (vertices.hasNext()) {
            Concept concept = new VertexConcept(vertices.next());
            if (vertices.hasNext()) {
                throw new RuntimeException("Too many \"" + VertexType.ENTITY + "\" concepts");
            }
            return concept;
        } else {
            throw new RuntimeException("Could not find \"" + VertexType.ENTITY + "\" concept");
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

    public List<String> getConceptPath(GraphSession graphSession, String conceptVertexId) {
        ArrayList<String> path = new ArrayList<String>();
        Vertex conceptVertex = graphSession.getGraph().getVertex(conceptVertexId);
        path.add((String) conceptVertex.getProperty(PropertyName.TITLE.toString()));
        while ((conceptVertex = getParentConceptVertex(conceptVertex)) != null) {
            path.add(0, (String) conceptVertex.getProperty(PropertyName.TITLE.toString()));
        }
        path.remove(0); // removes the "Entity" from the path.
        return path;
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
        return toRelationships(relationshipTypes);
    }

    private List<Relationship> toRelationships(List<Vertex> relationshipTypes) {
        ArrayList<Relationship> relationships = new ArrayList<Relationship>();
        for (Vertex relationshipType : relationshipTypes) {
            relationships.add(new VertexRelationship(relationshipType));
        }
        return relationships;
    }

    public List<Property> getPropertiesByConceptId(GraphSession graphSession, String conceptVertexId) {
        Vertex conceptVertex = graphSession.getGraph().getVertex(conceptVertexId);
        if (conceptVertex == null) {
            throw new RuntimeException("Could not find concept: " + conceptVertexId);
        }
        return getPropertiesByConceptId(graphSession, conceptVertex);
    }

    private List<Property> getPropertiesByConceptId(GraphSession graphSession, Vertex conceptVertex) {
        List<Property> properties = new ArrayList<Property>();

        Iterator<Vertex> propertyVertices = conceptVertex.getVertices(Direction.OUT, LabelName.HAS_PROPERTY.toString()).iterator();
        while (propertyVertices.hasNext()) {
            Vertex propertyVertex = propertyVertices.next();
            properties.add(new VertexProperty(propertyVertex));
        }

        Vertex parentConceptVertex = getParentConceptVertex(conceptVertex);
        if (parentConceptVertex != null) {
            List<Property> parentProperties = getPropertiesByConceptId(graphSession, parentConceptVertex);
            properties.addAll(parentProperties);
        }

        return properties;
    }
}
