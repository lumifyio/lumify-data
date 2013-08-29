package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.model.TitanGraphVertex;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.PropertyType;
import com.thinkaurelius.titan.core.TitanGraph;

import java.util.List;

public class Dev extends Base {
    public static final String PERSON_TYPE = "person";
    public static final String ORGANIZATION_TYPE = "organization";
    public static final String COMPANY_TYPE = "company";
    public static final String LOCATION_TYPE = "location";
    public static final String PHONE_NUMBER_TYPE = "phoneNumber";
    public static final String EMAIL_ADDRESS_TYPE = "emailAddress";

    public static void main(String[] args) throws Exception {
        new Dev().run(args);
    }

    @Override
    protected int defineOntology(TitanGraph graph, TitanGraphVertex entity) {
        List<String> palette = generateColorPalette(6);

        TitanGraphVertex person = getOrCreateConcept(graph, entity, PERSON_TYPE, "Person");
        person.setProperty(PropertyName.GLYPH_ICON.toString(), "bb94e4ffc8290bd0d07a919a010a4c5c69eb820d3ed4b6442da0fd217d74d058");
        person.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        addPropertyTo(graph, person, "birthDate", "birth date", PropertyType.DATE);

        TitanGraphVertex org = getOrCreateConcept(graph, entity, ORGANIZATION_TYPE, "Organization");
        org.setProperty(PropertyName.GLYPH_ICON.toString(), "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        org.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        addPropertyTo(graph, org, "formationDate", "formation date", PropertyType.DATE);

        TitanGraphVertex company = getOrCreateConcept(graph, org, COMPANY_TYPE, "Company");
        company.setProperty(PropertyName.GLYPH_ICON.toString(), "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        company.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        addPropertyTo(graph, company, "netIncome", "net income", PropertyType.CURRENCY);

        TitanGraphVertex location = getOrCreateConcept(graph, entity, LOCATION_TYPE, "Location");
        location.setProperty(PropertyName.GLYPH_ICON.toString(), "698ca9f70bbfce7bf7a4bdd5cdb8348d9062b31fa8085cc11b75f747e1b5c86b");
        location.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        addPropertyTo(graph, location, "geoLocation", "geo location", PropertyType.GEO_LOCATION);

        TitanGraphVertex phoneNumber = getOrCreateConcept(graph, entity, PHONE_NUMBER_TYPE, "Phone Number");
        phoneNumber.setProperty(PropertyName.GLYPH_ICON.toString(), "97979a0b19d327b519029fc0f7846a73d72961e6725d47391060fc956d4596dc");
        phoneNumber.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();

        TitanGraphVertex emailAddress = getOrCreateConcept(graph, entity, EMAIL_ADDRESS_TYPE, "Email Address");
        emailAddress.setProperty(PropertyName.GLYPH_ICON.toString(), "b2dd45fefb26bd75011fd58d22ee537b441c6e4840f5da556435227d6e191cd3");
        emailAddress.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        addPropertyTo(graph, emailAddress, "active", "active", PropertyType.STRING);

        getOrCreateRelationshipType(graph, person, person, "personKnowsPerson", "knows");
        addPropertyTo(graph, "personKnowsPerson", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, "personKnowsPerson", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        getOrCreateRelationshipType(graph, person, company, "personWorksAtCompany", "works at");
        addPropertyTo(graph, "personWorksAtCompany", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, "personWorksAtCompany", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        getOrCreateRelationshipType(graph, person, location, "personLivesAtLocation", "lives at");
        addPropertyTo(graph, "personLivesAtLocation", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, "personLivesAtLocation", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        getOrCreateRelationshipType(graph, person, emailAddress, "personHasEmailAddress", "has email address");
        addPropertyTo(graph, "personHasEmailAddress", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, "personHasEmailAddress", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        getOrCreateRelationshipType(graph, person, phoneNumber, "personHasPhoneNumber", "has phone number");
        addPropertyTo(graph, "personHasPhoneNumber", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, "personHasPhoneNumber", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        getOrCreateRelationshipType(graph, org, location, "orgHeadquarteredAtLocation", "headquartered at");
        addPropertyTo(graph, "orgHeadquarteredAtLocation", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, "orgHeadquarteredAtLocation", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        getOrCreateRelationshipType(graph, org, emailAddress, "orgHasEmailAddress", "has email address");
        addPropertyTo(graph, "orgHasEmailAddress", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, "orgHasEmailAddress", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        getOrCreateRelationshipType(graph, org, phoneNumber, "orgHasPhoneNumber", "has phone number");
        addPropertyTo(graph, "orgHasPhoneNumber", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, "orgHasPhoneNumber", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        return 0;
    }
}
