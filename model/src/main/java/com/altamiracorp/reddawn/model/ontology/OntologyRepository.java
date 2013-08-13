package com.altamiracorp.reddawn.model.ontology;

import com.altamiracorp.reddawn.model.GraphSession;
import com.altamiracorp.reddawn.model.graph.GraphNode;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OntologyRepository {
    public static final String TYPE_PROPERTY_NAME = "type";
    public static final String CONCEPT_PROPERTY_NAME = "concept";
    public static final String SUBTYPE_PROPERTY_NAME = "subType";
    public static final String TITLE_PROPERTY_NAME = "title";
    public static final String GEO_LOCATION_PROPERTY_NAME = "geoLocation";
    public static final String ROW_KEY_PROPERTY_NAME = "_rowKey";
    public static final String COLUMN_FAMILY_NAME_PROPERTY_NAME = "_columnFamilyName";

    public static final String CONCEPT_TYPE = "Concept";
    public static final String ARTIFACT_TYPE = "Artifact";
    public static final String TERM_MENTION_TYPE = "TermMention";
    public static final String ENTITY_TYPE = "Entity";

    public static final String HAS_PROPERTY_LABEL_NAME = "hasProperty";
    public static final String HAS_EDGE_LABEL_NAME = "hasEdge";
    public static final String IS_A_LABEL_NAME = "isA";
    public static final String HAS_TERM_MENTION_LABEL_NAME = "hasTermMention";

    public Concept getEntityConcept(GraphSession graphSession) {
        Iterator<Vertex> vertices = graphSession.getGraph().query()
                .has(TYPE_PROPERTY_NAME, CONCEPT_TYPE)
                .has(CONCEPT_PROPERTY_NAME, ENTITY_TYPE)
                .vertices()
                .iterator();
        if (vertices.hasNext()) {
            Concept concept = new VertexConcept(vertices.next());
            if (vertices.hasNext()) {
                throw new RuntimeException("Too many \"" + ENTITY_TYPE + "\" concepts");
            }
            return concept;
        } else {
            throw new RuntimeException("Could not find \"" + ENTITY_TYPE + "\" concept");
        }
    }

    public List<Concept> getChildConcepts(GraphSession graphSession, Concept concept) {
        Vertex conceptVertex = graphSession.getGraph().getVertex(concept.getId());
        return toConcepts(conceptVertex.getVertices(Direction.IN, IS_A_LABEL_NAME));
    }

    private List<Concept> toConcepts(Iterable<Vertex> vertices) {
        ArrayList<Concept> concepts = new ArrayList<Concept>();
        for (Vertex vertex : vertices) {
            concepts.add(new VertexConcept(vertex));
        }
        return concepts;
    }

    public List<String> getConceptPath(GraphSession graphSession, String conceptVertexId) {
        ArrayList<String> path = new ArrayList<String>();
        Vertex conceptVertex = graphSession.getGraph().getVertex(conceptVertexId);
        path.add((String) conceptVertex.getProperty(TITLE_PROPERTY_NAME));
        while ((conceptVertex = getParentConceptVertex(conceptVertex)) != null) {
            path.add(0, (String) conceptVertex.getProperty(TITLE_PROPERTY_NAME));
        }
        path.remove(0); // removes the "Entity" from the path.
        return path;
    }

    private Vertex getParentConceptVertex(Vertex conceptVertex) {
        Iterator<Vertex> parents = conceptVertex.getVertices(Direction.OUT, IS_A_LABEL_NAME).iterator();
        if (!parents.hasNext()) {
            return null;
        }
        Vertex v = parents.next();
        if (parents.hasNext()) {
            throw new RuntimeException("Unexpected number of parents for concept: " + conceptVertex.getProperty(TITLE_PROPERTY_NAME));
        }
        return v;
    }

    public Concept getConceptByName(GraphSession graphSession, String title) {
        GraphNode node = graphSession.findNodeByExactTitleAndType(title, CONCEPT_TYPE);
        if (node == null) {
            return null;
        }
        return new GraphNodeConcept(node);
    }
}
