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
        person.setProperty(PropertyName.GLYPH_ICON.toString(), "bb94e4ffc8290bd0d07a919a010a4c5c69eb820d3ed4b6442da0fd217d74d058");
        person.setProperty(PropertyName.COLOR.toString(), "rgb(0, 102, 255)");
        graph.commit();
        addPropertyToConcept(graph, person, "birthDate", PropertyType.DATE);
        addPropertyToConcept(graph, person, PropertyName.GLYPH_ICON.toString(), PropertyType.IMAGE);

        TitanVertex org = getOrCreateConcept(graph, entity, ORGANIZATION_TYPE);
        org.setProperty(PropertyName.GLYPH_ICON.toString(), "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        org.setProperty(PropertyName.COLOR.toString(), "rgb(0, 255, 102)");
        graph.commit();
        addPropertyToConcept(graph, org, "formationDate", PropertyType.DATE);

        TitanVertex company = getOrCreateConcept(graph, org, COMPANY_TYPE);
        company.setProperty(PropertyName.GLYPH_ICON.toString(), "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        company.setProperty(PropertyName.COLOR.toString(), "rgb(0, 255, 102)");
        graph.commit();
        addPropertyToConcept(graph, company, "netIncome", PropertyType.CURRENCY);

        TitanVertex location = getOrCreateConcept(graph, entity, LOCATION_TYPE);
        location.setProperty(PropertyName.GLYPH_ICON.toString(), "698ca9f70bbfce7bf7a4bdd5cdb8348d9062b31fa8085cc11b75f747e1b5c86b");
        location.setProperty(PropertyName.COLOR.toString(), "rgb(204, 255, 0)");
        graph.commit();
        addPropertyToConcept(graph, location, "geoLocation", PropertyType.GEO_LOCATION);

        getOrCreateRelationshipType(graph, person, person, "knows");
        getOrCreateRelationshipType(graph, person, company, "worksAt");
        getOrCreateRelationshipType(graph, person, location, "livesAt");
        getOrCreateRelationshipType(graph, org, location, "headquarteredAt");

        return 0;
    }
}
