package com.altamiracorp.reddawn.model.ontology;

import com.altamiracorp.reddawn.model.GraphSession;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OntologyRepository {
    public static final String CONCEPT_TYPE = "Concept";
    public static final String ARTIFACT_TYPE = "Artifact";
    public static final String TERM_MENTION_TYPE = "TermMention";
    public static final String ENTITY_TYPE = "Entity";

    public Concept getEntityConcept(GraphSession graphSession) {
        Iterator<Vertex> vertices = graphSession.getGraph().query()
                .has("type", CONCEPT_TYPE)
                .has("concept", ENTITY_TYPE)
                .vertices()
                .iterator();
        if (vertices.hasNext()) {
            Concept concept = new Concept(vertices.next());
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
        return toConcepts(conceptVertex.getVertices(Direction.IN, "isA"));
    }

    private List<Concept> toConcepts(Iterable<Vertex> vertices) {
        ArrayList<Concept> concepts = new ArrayList<Concept>();
        for (Vertex vertex : vertices) {
            concepts.add(new Concept(vertex));
        }
        return concepts;
    }
}
