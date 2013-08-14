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
        person.setProperty(OntologyRepository.GLYPH_ICON_PROPERTY_NAME, "bb94e4ffc8290bd0d07a919a010a4c5c69eb820d3ed4b6442da0fd217d74d058");
        person.setProperty(OntologyRepository.COLOR_PROPERTY_NAME, "rgb(0, 102, 255)");

        TitanVertex org = getOrCreateType(graph, entity, ORGANIZATION_TYPE);
        org.setProperty(OntologyRepository.GLYPH_ICON_PROPERTY_NAME, "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        org.setProperty(OntologyRepository.COLOR_PROPERTY_NAME, "rgb(0, 255, 102)");
        TitanVertex company = getOrCreateType(graph, org, COMPANY_TYPE);
        company.setProperty(OntologyRepository.GLYPH_ICON_PROPERTY_NAME, "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        company.setProperty(OntologyRepository.COLOR_PROPERTY_NAME, "rgb(0, 255, 102)");

        TitanVertex location = getOrCreateType(graph, entity, LOCATION_TYPE);
        location.setProperty(OntologyRepository.GLYPH_ICON_PROPERTY_NAME, "698ca9f70bbfce7bf7a4bdd5cdb8348d9062b31fa8085cc11b75f747e1b5c86b");
        location.setProperty(OntologyRepository.COLOR_PROPERTY_NAME, "rgb(204, 255, 0)");

        graph.commit();

        getOrCreateRelationshipType(graph, person, person, KNOWS_RELATIONSHIP_TYPE);
        getOrCreateRelationshipType(graph, person, company, WORKS_AT_RELATIONSHIP_TYPE);

        return 0;
    }
}
