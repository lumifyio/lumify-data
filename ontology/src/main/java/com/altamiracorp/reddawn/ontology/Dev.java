package com.altamiracorp.reddawn.ontology;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

public class Dev extends Base {
    public static final String PERSON_TYPE = "Person";
    public static final String ORGANIZATION_TYPE = "Organization";
    public static final String COMPANY_TYPE = "Company";
    public static final String LOCATION_TYPE = "Location";

    public static final String KNOWS_RELATIONSHIP_TYPE = "knows";
    public static final String WORKS_AT_RELATIONSHIP_TYPE = "worksAt";

    public static void main(String[] args) throws Exception {
        new Dev().run(args);
    }

    @Override
    protected int defineOntology(TitanGraph graph, TitanVertex entity) {
        TitanVertex person = getOrCreateType(graph, entity, PERSON_TYPE);
        TitanVertex org = getOrCreateType(graph, entity, ORGANIZATION_TYPE);
        TitanVertex company = getOrCreateType(graph, org, COMPANY_TYPE);
        TitanVertex location = getOrCreateType(graph, entity, LOCATION_TYPE);

        getOrCreateRelationshipType(graph, person, person, KNOWS_RELATIONSHIP_TYPE);
        getOrCreateRelationshipType(graph, person, company, WORKS_AT_RELATIONSHIP_TYPE);

        return 0;
    }
}
