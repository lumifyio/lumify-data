package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanLabel;
import com.thinkaurelius.titan.core.TitanVertex;
import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;

public class Dev extends Base {
    public static final String PERSON_TYPE = "Person";
    public static final String ORGANIZATION_TYPE = "Organization";

    public static void main(String[] args) throws Exception {
        new Dev().run(args);
    }

    @Override
    protected int defineOntology(TitanGraph graph, TitanVertex entity) {
        TitanKey conceptProperty = properties.get("concept");
        TitanKey typeProperty = properties.get("type");
        TitanKey titleProperty = properties.get("title");
        TitanLabel hasPropertyEdge = edges.get("hasProperty");
        TitanLabel isAEdge = edges.get("isA");

        Iterator<Vertex> personIter = graph.getVertices(conceptProperty.getName(), PERSON_TYPE).iterator();
        TitanVertex person;
        if (personIter.hasNext()) {
            person = (TitanVertex) personIter.next();
        } else {
            person = (TitanVertex) graph.addVertex(null);
            person.setProperty(typeProperty, OntologyRepository.CONCEPT_TYPE);
            person.setProperty(titleProperty, "Person");
            person.addProperty(conceptProperty, PERSON_TYPE);
            person.addEdge(hasPropertyEdge, typeProperty);
            person.addEdge(isAEdge.getName(), entity);
        }

        Iterator<Vertex> orgIter = graph.getVertices(conceptProperty.getName(), ORGANIZATION_TYPE).iterator();
        TitanVertex org;
        if (orgIter.hasNext()) {
            org = (TitanVertex) orgIter.next();
        } else {
            org = (TitanVertex) graph.addVertex(null);
            org.setProperty(typeProperty, OntologyRepository.CONCEPT_TYPE);
            org.setProperty(titleProperty, "Organization");
            org.addProperty(conceptProperty, ORGANIZATION_TYPE);
            org.addEdge(hasPropertyEdge, typeProperty);
            org.addEdge(isAEdge.getName(), entity);
        }

        return 0;
    }
}
