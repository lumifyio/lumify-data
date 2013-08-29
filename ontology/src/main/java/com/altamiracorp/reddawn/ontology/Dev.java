package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.ontology.Concept;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
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

    private OntologyRepository ontologyRepository = new OntologyRepository();

    public static void main(String[] args) throws Exception {
        new Dev().run(args);
    }

    @Override
    protected int defineOntology(RedDawnSession redDawnSession, TitanGraph graph, Concept entity) {
        List<String> palette = generateColorPalette(6);

        Concept person = ontologyRepository.getOrCreateConcept(redDawnSession.getGraphSession(), entity, PERSON_TYPE, "Person");
        person.setProperty(PropertyName.GLYPH_ICON.toString(), "bb94e4ffc8290bd0d07a919a010a4c5c69eb820d3ed4b6442da0fd217d74d058");
        person.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), person, "birthDate", "birth date", PropertyType.DATE);

        Concept org = ontologyRepository.getOrCreateConcept(redDawnSession.getGraphSession(), entity, ORGANIZATION_TYPE, "Organization");
        org.setProperty(PropertyName.GLYPH_ICON.toString(), "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        org.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), org, "formationDate", "formation date", PropertyType.DATE);

        Concept company = ontologyRepository.getOrCreateConcept(redDawnSession.getGraphSession(), org, COMPANY_TYPE, "Company");
        company.setProperty(PropertyName.GLYPH_ICON.toString(), "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        company.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), company, "netIncome", "net income", PropertyType.CURRENCY);

        Concept location = ontologyRepository.getOrCreateConcept(redDawnSession.getGraphSession(), entity, LOCATION_TYPE, "Location");
        location.setProperty(PropertyName.GLYPH_ICON.toString(), "698ca9f70bbfce7bf7a4bdd5cdb8348d9062b31fa8085cc11b75f747e1b5c86b");
        location.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), location, "geoLocation", "geo location", PropertyType.GEO_LOCATION);

        Concept phoneNumber = ontologyRepository.getOrCreateConcept(redDawnSession.getGraphSession(), entity, PHONE_NUMBER_TYPE, "Phone Number");
        phoneNumber.setProperty(PropertyName.GLYPH_ICON.toString(), "97979a0b19d327b519029fc0f7846a73d72961e6725d47391060fc956d4596dc");
        phoneNumber.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();

        Concept emailAddress = ontologyRepository.getOrCreateConcept(redDawnSession.getGraphSession(), entity, EMAIL_ADDRESS_TYPE, "Email Address");
        emailAddress.setProperty(PropertyName.GLYPH_ICON.toString(), "b2dd45fefb26bd75011fd58d22ee537b441c6e4840f5da556435227d6e191cd3");
        emailAddress.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), emailAddress, "active", "active", PropertyType.STRING);

        ontologyRepository.getOrCreateRelationshipType(redDawnSession.getGraphSession(), person, person, "personKnowsPerson", "knows");
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "personKnowsPerson", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "personKnowsPerson", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        ontologyRepository.getOrCreateRelationshipType(redDawnSession.getGraphSession(), person, company, "personWorksAtCompany", "works at");
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "personWorksAtCompany", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "personWorksAtCompany", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        ontologyRepository.getOrCreateRelationshipType(redDawnSession.getGraphSession(), person, location, "personLivesAtLocation", "lives at");
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "personLivesAtLocation", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "personLivesAtLocation", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        ontologyRepository.getOrCreateRelationshipType(redDawnSession.getGraphSession(), person, emailAddress, "personHasEmailAddress", "has email address");
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "personHasEmailAddress", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "personHasEmailAddress", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        ontologyRepository.getOrCreateRelationshipType(redDawnSession.getGraphSession(), person, phoneNumber, "personHasPhoneNumber", "has phone number");
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "personHasPhoneNumber", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "personHasPhoneNumber", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        ontologyRepository.getOrCreateRelationshipType(redDawnSession.getGraphSession(), org, location, "orgHeadquarteredAtLocation", "headquartered at");
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "orgHeadquarteredAtLocation", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "orgHeadquarteredAtLocation", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        ontologyRepository.getOrCreateRelationshipType(redDawnSession.getGraphSession(), org, emailAddress, "orgHasEmailAddress", "has email address");
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "orgHasEmailAddress", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "orgHasEmailAddress", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        ontologyRepository.getOrCreateRelationshipType(redDawnSession.getGraphSession(), org, phoneNumber, "orgHasPhoneNumber", "has phone number");
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "orgHasPhoneNumber", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), "orgHasPhoneNumber", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE);

        return 0;
    }
}
