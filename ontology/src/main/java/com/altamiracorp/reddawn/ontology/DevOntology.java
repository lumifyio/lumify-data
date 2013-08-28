package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.model.TitanGraphVertex;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.PropertyType;
import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.google.common.collect.Maps;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanLabel;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class DevOntology {
    public static final String PERSON_TYPE = "person";
    public static final String ORGANIZATION_TYPE = "organization";
    public static final String COMPANY_TYPE = "company";
    public static final String LOCATION_TYPE = "location";
    public static final String PHONE_NUMBER_TYPE = "phoneNumber";
    public static final String EMAIL_ADDRESS_TYPE = "emailAddress";
    private final TitanGraph graph;
    private final OntologyBuilder builder;
    private final Map<String, TitanKey> properties = Maps.newHashMap();
    private final Map<String, TitanLabel> edges = Maps.newHashMap();

    public DevOntology(TitanGraph graph) {
        checkNotNull(graph);
        checkArgument(graph.isOpen());
        this.graph = graph;
        this.builder = new OntologyBuilder(graph);
    }

    public static void main(String[] args) throws Exception {
    }

    public void defineOntology() {
        TitanGraphVertex rootConcept = builder.getOrCreateConcept(null, OntologyRepository.ROOT_CONCEPT_NAME, OntologyRepository.ROOT_CONCEPT_NAME);
        TitanGraphVertex entity = builder.getOrCreateConcept(rootConcept, VertexType.ENTITY.toString(), VertexType.ENTITY.toString());

        List<String> palette = builder.generateColorPalette(6);

        TitanGraphVertex person = builder.getOrCreateConcept(entity, PERSON_TYPE, "Person");
        person.setProperty(PropertyName.GLYPH_ICON.toString(), "bb94e4ffc8290bd0d07a919a010a4c5c69eb820d3ed4b6442da0fd217d74d058");
        person.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        builder.addPropertyTo(person, "birthDate", "birth date", PropertyType.DATE, properties);

        TitanGraphVertex org = builder.getOrCreateConcept(entity, ORGANIZATION_TYPE, "Organization");
        org.setProperty(PropertyName.GLYPH_ICON.toString(), "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        org.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        builder.addPropertyTo(org, "formationDate", "formation date", PropertyType.DATE, properties);

        TitanGraphVertex company = builder.getOrCreateConcept(org, COMPANY_TYPE, "Company");
        company.setProperty(PropertyName.GLYPH_ICON.toString(), "8777a8592b14db5a7d4d151d9887f9500077adbfac7e30fecd987093299602da");
        company.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        builder.addPropertyTo(company, "netIncome", "net income", PropertyType.CURRENCY, properties);

        TitanGraphVertex location = builder.getOrCreateConcept(entity, LOCATION_TYPE, "Location");
        location.setProperty(PropertyName.GLYPH_ICON.toString(), "698ca9f70bbfce7bf7a4bdd5cdb8348d9062b31fa8085cc11b75f747e1b5c86b");
        location.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        builder.addPropertyTo(location, "geoLocation", "geo location", PropertyType.GEO_LOCATION, properties);

        TitanGraphVertex phoneNumber = builder.getOrCreateConcept(entity, PHONE_NUMBER_TYPE, "Phone Number");
        phoneNumber.setProperty(PropertyName.GLYPH_ICON.toString(), "97979a0b19d327b519029fc0f7846a73d72961e6725d47391060fc956d4596dc");
        phoneNumber.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();

        TitanGraphVertex emailAddress = builder.getOrCreateConcept(entity, EMAIL_ADDRESS_TYPE, "Email Address");
        emailAddress.setProperty(PropertyName.GLYPH_ICON.toString(), "b2dd45fefb26bd75011fd58d22ee537b441c6e4840f5da556435227d6e191cd3");
        emailAddress.setProperty(PropertyName.COLOR.toString(), palette.remove(0));
        graph.commit();
        builder.addPropertyTo(emailAddress, "active", "active", PropertyType.STRING, properties);

        builder.getOrCreateRelationshipType(person, person, "knows", "knows", edges);
        builder.getOrCreateRelationshipType(person, company, "worksAt", "works at", edges);
        builder.getOrCreateRelationshipType(person, location, "livesAt", "lives at", edges);
        builder.getOrCreateRelationshipType(person, emailAddress, "hasEmailAddress", "has email address", edges);
        builder.getOrCreateRelationshipType(person, phoneNumber, "hasPhoneNumber", "has phone number", edges);
        builder.getOrCreateRelationshipType(org, location, "headquarteredAt", "headquartered at", edges);
        builder.getOrCreateRelationshipType(org, emailAddress, "hasEmailAddress", "has email address", edges);
        builder.getOrCreateRelationshipType(org, phoneNumber, "hasPhoneNumber", "has phone number", edges);
        builder.getOrCreateRelationshipType(company, location, "headquarteredAt", "headquartered at", edges);
        builder.getOrCreateRelationshipType(company, emailAddress, "hasEmailAddress", "has email address", edges);
        builder.getOrCreateRelationshipType(company, phoneNumber, "hasPhoneNumber", "has phone number", edges);

        builder.addPropertyTo("knows", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE, properties);
        builder.addPropertyTo("knows", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE, properties);
        builder.addPropertyTo("worksAt", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE, properties);
        builder.addPropertyTo("worksAt", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE, properties);
        builder.addPropertyTo("livesAt", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE, properties);
        builder.addPropertyTo("livesAt", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE, properties);
        builder.addPropertyTo("hasEmailAddress", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE, properties);
        builder.addPropertyTo("hasEmailAddress", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE, properties);
        builder.addPropertyTo("headquarteredAt", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE, properties);
        builder.addPropertyTo("headquarteredAt", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE, properties);
        builder.addPropertyTo("hasPhoneNumber", PropertyName.START_DATE.toString(), "Start date", PropertyType.DATE, properties);
        builder.addPropertyTo("hasPhoneNumber", PropertyName.END_DATE.toString(), "End date", PropertyType.DATE, properties);

        graph.commit();
        graph.shutdown();
    }
}
