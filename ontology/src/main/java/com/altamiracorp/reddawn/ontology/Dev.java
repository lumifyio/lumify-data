package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.PropertyType;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

public class Dev extends Base {
    public static final String PERSON_TYPE = "Person";
    public static final String ORGANIZATION_TYPE = "Organization";
    public static final String COMPANY_TYPE = "Company";
    public static final String LOCATION_TYPE = "Location";

    public static void main(String[] args) throws Exception {
        new Dev().run(args);
    }

    @Override
    protected int defineOntology(TitanGraph graph, TitanVertex entity) {
        TitanVertex person = getOrCreateConcept(graph, entity, PERSON_TYPE);
        addPropertyToConcept(graph, person, "birthDate", PropertyType.DATE);
        person.setProperty(PropertyName.GLYPH_ICON.toString(), "f3ee34c83989653f9ed5abf9c1d138abda75951b0e9682b7e6965f545d6ebf20");
        person.setProperty(PropertyName.COLOR.toString(), "rgba(0, 102, 255, 0.2)");

        TitanVertex org = getOrCreateConcept(graph, entity, ORGANIZATION_TYPE);
        org.setProperty(PropertyName.GLYPH_ICON.toString(), "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        org.setProperty(PropertyName.COLOR.toString(), "rgba(0, 255, 102, 0.2)");
        addPropertyToConcept(graph, org, "formationDate", PropertyType.DATE);

        TitanVertex company = getOrCreateConcept(graph, org, COMPANY_TYPE);
        company.setProperty(PropertyName.GLYPH_ICON.toString(), "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        company.setProperty(PropertyName.COLOR.toString(), "rgba(0, 255, 102, 0.2)");
        addPropertyToConcept(graph, company, "netIncome", PropertyType.CURRENCY);

        TitanVertex location = getOrCreateConcept(graph, entity, LOCATION_TYPE);
        location.setProperty(PropertyName.GLYPH_ICON.toString(), "caffdc4a603c968ca4a6392aeceaca380c02231459d9ba7240f807eaf0775c65");
        location.setProperty(PropertyName.COLOR.toString(), "rgba(204, 255, 0, 0.2)");
        addPropertyToConcept(graph, location, "geoLocation", PropertyType.GEO_LOCATION);

        graph.commit();

        getOrCreateRelationshipType(graph, person, person, "knows");
        getOrCreateRelationshipType(graph, person, company, "worksAt");
        getOrCreateRelationshipType(graph, person, location, "livesAt");
        getOrCreateRelationshipType(graph, org, location, "headquarteredAt");

        return 0;
    }
}
