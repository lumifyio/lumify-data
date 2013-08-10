package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.model.ontology.ConceptType;
import com.thinkaurelius.titan.core.*;
import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;

public class Dev extends Base {
    public static final String PERSON_TYPE = "Person";

    public static void main(String[] args) throws Exception {
        new Dev().run(args);
    }

    @Override
    protected int defineOntology(TitanGraph graph) {
        Vertex entity = graph.getVertices("concept", ConceptType.Entity.toString()).iterator().next();
        TitanKey conceptProperty = properties.get("concept");
        TitanKey typeProperty = properties.get("type");
        TitanLabel hasPropertyEdge = edges.get("hasProperty");
        TitanLabel isAEdge = edges.get("isA");

        Iterator<Vertex> personIter = graph.getVertices(conceptProperty.getName(), PERSON_TYPE).iterator();
        TitanVertex person;
        if (personIter.hasNext()) {
            person = (TitanVertex) personIter.next();
        } else {
            person = (TitanVertex) graph.addVertex(null);
            person.setProperty(typeProperty, CONCEPT_TYPE);
            person.addProperty(conceptProperty, PERSON_TYPE);
            person.addEdge(hasPropertyEdge, typeProperty);
            person.addEdge(isAEdge.getName(), entity);
        }

        return 0;
    }
}
