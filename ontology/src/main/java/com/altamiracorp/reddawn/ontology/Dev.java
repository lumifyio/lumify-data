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

        getOrCreateRelationshipType(graph, person, person, "knows", "knows");
        getOrCreateRelationshipType(graph, person, company, "worksAt", "works at");
        getOrCreateRelationshipType(graph, person, location, "livesAt", "lives at");
        getOrCreateRelationshipType(graph, person, emailAddress, "hasEmailAddress", "has email address");
        getOrCreateRelationshipType(graph, person, phoneNumber, "hasPhoneNumber", "has phone number");
        getOrCreateRelationshipType(graph, org, location, "headquarteredAt", "headquartered at");
        getOrCreateRelationshipType(graph, org, emailAddress, "hasEmailAddress", "has email address");
        getOrCreateRelationshipType(graph, org, phoneNumber, "hasPhoneNumber", "has phone number");
        getOrCreateRelationshipType(graph, company, location, "headquarteredAt", "headquartered at");
        getOrCreateRelationshipType(graph, company, emailAddress, "hasEmailAddress", "has email address");
        getOrCreateRelationshipType(graph, company, phoneNumber, "hasPhoneNumber", "has phone number");

        TitanGraphVertex knows = getRelationship(graph,"knows", "knows");
        TitanGraphVertex worksAt = getRelationship(graph,"worksAt", "works at");
        TitanGraphVertex livesAt = getRelationship(graph, "livesAt", "lives at");
        TitanGraphVertex hasEmailAddress = getRelationship(graph, "hasEmailAddress", "has email address");
        TitanGraphVertex headquarteredAt = getRelationship(graph, "headquarteredAt", "headquartered at");
        TitanGraphVertex hasPhoneNumber = getRelationship(graph, "hasPhoneNumber", "has phone number");

        addPropertyTo(graph, knows, PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, knows, PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);
        addPropertyTo(graph, worksAt, PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, worksAt, PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);
        addPropertyTo(graph, livesAt, PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, livesAt, PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);
        addPropertyTo(graph, hasEmailAddress, PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, hasEmailAddress, PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);
        addPropertyTo(graph, headquarteredAt, PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, headquarteredAt, PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);
        addPropertyTo(graph, hasPhoneNumber, PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        addPropertyTo(graph, hasPhoneNumber, PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);


        return 0;
    }
}
