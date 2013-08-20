package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.PropertyType;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

import java.util.List;

public class Dev extends Base {
    public static final String PERSON_TYPE = "Person";
    public static final String ORGANIZATION_TYPE = "Organization";
    public static final String COMPANY_TYPE = "Company";
    public static final String LOCATION_TYPE = "Location";
    public static final String PHONE_NUMBER_TYPE = "Phone Number";
    public static final String EMAIL_ADDRESS_TYPE = "Email Address";

    public static void main(String[] args) throws Exception {
        new Dev().run(args);
    }

    @Override
    protected int defineOntology(TitanGraph graph, TitanVertex entity) {
        List<String> palette = generateColorPalette(6);

        TitanVertex person = getOrCreateConcept(graph, entity, PERSON_TYPE);
        person.setProperty(PropertyName.GLYPH_ICON.toString(), "bb94e4ffc8290bd0d07a919a010a4c5c69eb820d3ed4b6442da0fd217d74d058");
        person.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        addPropertyToConcept(graph, person, "birthDate", PropertyType.DATE);

        TitanVertex org = getOrCreateConcept(graph, entity, ORGANIZATION_TYPE);
        org.setProperty(PropertyName.GLYPH_ICON.toString(), "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        org.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        addPropertyToConcept(graph, org, "formationDate", PropertyType.DATE);

        TitanVertex company = getOrCreateConcept(graph, org, COMPANY_TYPE);
        company.setProperty(PropertyName.GLYPH_ICON.toString(), "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        company.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        addPropertyToConcept(graph, company, "netIncome", PropertyType.CURRENCY);

        TitanVertex location = getOrCreateConcept(graph, entity, LOCATION_TYPE);
        location.setProperty(PropertyName.GLYPH_ICON.toString(), "698ca9f70bbfce7bf7a4bdd5cdb8348d9062b31fa8085cc11b75f747e1b5c86b");
        location.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        addPropertyToConcept(graph, location, "geoLocation", PropertyType.GEO_LOCATION);

        TitanVertex phoneNumber = getOrCreateConcept(graph, entity, PHONE_NUMBER_TYPE);
        phoneNumber.setProperty(PropertyName.GLYPH_ICON.toString(), "97979a0b19d327b519029fc0f7846a73d72961e6725d47391060fc956d4596dc");
        phoneNumber.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();

        TitanVertex emailAddress = getOrCreateConcept(graph,entity, EMAIL_ADDRESS_TYPE);
        emailAddress.setProperty(PropertyName.GLYPH_ICON.toString(), "b2dd45fefb26bd75011fd58d22ee537b441c6e4840f5da556435227d6e191cd3");
        emailAddress.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        addPropertyToConcept(graph,emailAddress,"active", PropertyType.STRING);

        getOrCreateRelationshipType(graph, person, person, "knows");
        getOrCreateRelationshipType(graph, person, company, "worksAt");
        getOrCreateRelationshipType(graph, person, location, "livesAt");
        getOrCreateRelationshipType(graph, org, location, "headquarteredAt");
        getOrCreateRelationshipType(graph, org, emailAddress, "hasEmailAddress");
        getOrCreateRelationshipType(graph, org, phoneNumber, "hasPhoneNumber");
        getOrCreateRelationshipType(graph, person, emailAddress, "hasEmailAddress");
        getOrCreateRelationshipType(graph, person, phoneNumber, "hasPhoneNumber");
        getOrCreateRelationshipType(graph, company, location, "headquarteredAt");
        getOrCreateRelationshipType(graph, company, emailAddress, "hasEmailAddress");
        getOrCreateRelationshipType(graph, company, phoneNumber, "hasPhoneNumber");

        return 0;
    }
}
