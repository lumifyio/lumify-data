package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.model.TitanGraphVertex;
import com.altamiracorp.reddawn.model.ontology.*;
import com.google.common.collect.Maps;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanLabel;
import com.thinkaurelius.titan.core.attribute.Geoshape;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class BaseOntology {
    private final OntologyBuilder builder;
    private final Map<String, TitanKey> properties = Maps.newHashMap();
    private final Map<String, TitanLabel> edges = Maps.newHashMap();
    private final TitanGraph graph;

    public BaseOntology(TitanGraph graph) {
        checkNotNull(graph);
        checkArgument(graph.isOpen());
        this.graph = graph;
        this.builder = new OntologyBuilder(graph);
    }

    public void defineOntology() {
        // concept properties
        TitanKey typeProperty = builder.getOrCreateTitanKey(PropertyName.TYPE, properties);
        builder.getOrCreateTitanKey(PropertyName.DATA_TYPE, properties);
        builder.getOrCreateTitanKey(PropertyName.ONTOLOGY_TITLE, properties);

        // concept edges
        builder.createConceptEdge(LabelName.HAS_PROPERTY, edges);
        builder.createConceptEdge(LabelName.HAS_EDGE, edges);
        builder.createConceptEdge(LabelName.IS_A, edges);

        builder.getOrCreateTitanKey(PropertyName.RELATIONSHIP_TYPE, properties);
        TitanKey subTypeProperty = builder.getOrCreateTitanKey(PropertyName.SUBTYPE, properties);

        builder.getOrCreateTitanKey(PropertyName.DISPLAY_NAME, properties);
        TitanKey titleProperty = builder.getOrCreateTitanKey(PropertyName.TITLE, properties);
        builder.getOrCreateTitanKey(PropertyName.GLYPH_ICON, properties);

        builder.getOrCreateTitanKey(PropertyName.COLOR, properties);
        TitanKey geoLocationProperty = builder.getOrCreateTitanKey(PropertyName.GEO_LOCATION, properties, Geoshape.class);

        graph.commit();

        TitanGraphVertex rootConcept = builder.getOrCreateConcept(null, OntologyRepository.ROOT_CONCEPT_NAME, OntologyRepository.ROOT_CONCEPT_NAME);
        graph.commit();

        TitanGraphVertex artifact = builder.getOrCreateConcept(rootConcept, VertexType.ARTIFACT.toString(), "Artifact");
        builder.addPropertyTo(artifact, typeProperty.getName(), "Type", PropertyType.STRING, properties);
        builder.addPropertyTo(artifact, subTypeProperty.getName(), "Subtype", PropertyType.STRING, properties);
        builder.addPropertyTo(artifact, titleProperty.getName(), "Title", PropertyType.STRING, properties);
        builder.addPropertyTo(artifact, geoLocationProperty.getName(), "Geo-location", PropertyType.GEO_LOCATION, properties);
        graph.commit();

        // TermMention concept
        builder.getOrCreateTitanKey(PropertyName.ROW_KEY, properties);
        builder.getOrCreateTitanKey(PropertyName.SOURCE, properties);
        graph.commit();

        // Entity concept
        TitanGraphVertex entity = builder.getOrCreateConcept(rootConcept, VertexType.ENTITY.toString(), VertexType.ENTITY.toString());
        builder.addPropertyTo(entity, typeProperty.getName(), "Type", PropertyType.STRING, properties);
        builder.addPropertyTo(entity, subTypeProperty.getName(), "Subtype", PropertyType.STRING, properties);
        builder.addPropertyTo(entity, titleProperty.getName(), "Title", PropertyType.STRING, properties);
        builder.addPropertyTo(entity, PropertyName.GLYPH_ICON.toString(), "glyph icon", PropertyType.IMAGE, properties);

        builder.getOrCreateRelationshipType(entity, artifact, LabelName.HAS_IMAGE.toString(), "has image", edges);
        graph.commit();

        // Artifact to TermMention relationship
        builder.getOrCreateRelationshipType(artifact, entity, LabelName.HAS_ENTITY.toString(), "has entity", edges);
        graph.commit();
        graph.shutdown();
    }

    public boolean isOntologyDefined() {
        return graph.getType(PropertyName.SOURCE.toString()) != null; // Should check for more
    }
}
