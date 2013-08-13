package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
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
        person.setProperty(OntologyRepository.GLYPH_ICON_PROPERTY_NAME, "f3ee34c83989653f9ed5abf9c1d138abda75951b0e9682b7e6965f545d6ebf20");

        TitanVertex org = getOrCreateType(graph, entity, ORGANIZATION_TYPE);
        org.setProperty(OntologyRepository.GLYPH_ICON_PROPERTY_NAME, "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        TitanVertex company = getOrCreateType(graph, org, COMPANY_TYPE);
        company.setProperty(OntologyRepository.GLYPH_ICON_PROPERTY_NAME, "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");

        TitanVertex location = getOrCreateType(graph, entity, LOCATION_TYPE);
        location.setProperty(OntologyRepository.GLYPH_ICON_PROPERTY_NAME, "caffdc4a603c968ca4a6392aeceaca380c02231459d9ba7240f807eaf0775c65");

        graph.commit();

        getOrCreateRelationshipType(graph, person, person, KNOWS_RELATIONSHIP_TYPE);
        getOrCreateRelationshipType(graph, person, company, WORKS_AT_RELATIONSHIP_TYPE);

        return 0;
    }
}
