package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.model.TitanGraphVertex;
import com.altamiracorp.reddawn.model.ontology.*;
import com.google.common.collect.Maps;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanLabel;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

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
        TitanKey typeProperty = (TitanKey) graph.getType(PropertyName.TYPE.toString());
        if (typeProperty == null) {
            typeProperty = graph.makeType().name(PropertyName.TYPE.toString()).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(typeProperty.getName(), typeProperty);

        TitanKey dataTypeProperty = (TitanKey) graph.getType(PropertyName.DATA_TYPE.toString());
        if (dataTypeProperty == null) {
            dataTypeProperty = graph.makeType().name(PropertyName.DATA_TYPE.toString()).dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }
        properties.put(dataTypeProperty.getName(), dataTypeProperty);

        TitanKey ontologyTitleProperty = (TitanKey) graph.getType(PropertyName.ONTOLOGY_TITLE.toString());
        if (ontologyTitleProperty == null) {
            ontologyTitleProperty = graph.makeType().name(PropertyName.ONTOLOGY_TITLE.toString()).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(ontologyTitleProperty.getName(), ontologyTitleProperty);

        // concept edges
        TitanLabel hasPropertyEdge = (TitanLabel) graph.getType(LabelName.HAS_PROPERTY.toString());
        if (hasPropertyEdge == null) {
            hasPropertyEdge = graph.makeType().name(LabelName.HAS_PROPERTY.toString()).directed().makeEdgeLabel();
        }
        edges.put(hasPropertyEdge.getName(), hasPropertyEdge);

        TitanLabel hasEdgeEdge = (TitanLabel) graph.getType(LabelName.HAS_EDGE.toString());
        if (hasEdgeEdge == null) {
            hasEdgeEdge = graph.makeType().name(LabelName.HAS_EDGE.toString()).directed().makeEdgeLabel();
        }
        edges.put(hasEdgeEdge.getName(), hasEdgeEdge);

        TitanLabel isAEdge = (TitanLabel) graph.getType(LabelName.IS_A.toString());
        if (isAEdge == null) {
            isAEdge = graph.makeType().name(LabelName.IS_A.toString()).directed().makeEdgeLabel();
        }
        edges.put(isAEdge.getName(), isAEdge);

        TitanKey relationshipType = (TitanKey) graph.getType(PropertyName.RELATIONSHIP_TYPE.toString());
        if (relationshipType == null) {
            relationshipType = graph.makeType().name(PropertyName.RELATIONSHIP_TYPE.toString()).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(relationshipType.getName(), relationshipType);

        TitanKey subTypeProperty = (TitanKey) graph.getType(PropertyName.SUBTYPE.toString());
        if (subTypeProperty == null) {
            subTypeProperty = graph.makeType().name(PropertyName.SUBTYPE.toString()).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(subTypeProperty.getName(), subTypeProperty);

        TitanKey displayNameProperty = (TitanKey) graph.getType(PropertyName.DISPLAY_NAME.toString());
        if (displayNameProperty == null) {
            displayNameProperty = graph.makeType().name(PropertyName.DISPLAY_NAME.toString()).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(displayNameProperty.getName(), displayNameProperty);

        TitanKey titleProperty = (TitanKey) graph.getType(PropertyName.TITLE.toString());
        if (titleProperty == null) {
            titleProperty = graph.makeType().name(PropertyName.TITLE.toString()).dataType(String.class).unique(Direction.OUT).indexed("search", Vertex.class).makePropertyKey();
        }
        properties.put(titleProperty.getName(), titleProperty);

        TitanKey glyphIconProperty = (TitanKey) graph.getType(PropertyName.GLYPH_ICON.toString());
        if (glyphIconProperty == null) {
            glyphIconProperty = graph.makeType().name(PropertyName.GLYPH_ICON.toString()).dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }
        properties.put(glyphIconProperty.getName(), glyphIconProperty);

        TitanKey colorProperty = (TitanKey) graph.getType(PropertyName.COLOR.toString());
        if (colorProperty == null) {
            colorProperty = graph.makeType().name(PropertyName.COLOR.toString()).dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }
        properties.put(colorProperty.getName(), colorProperty);

        TitanKey geoLocationProperty = (TitanKey) graph.getType(PropertyName.GEO_LOCATION.toString());
        if (geoLocationProperty == null) {
            geoLocationProperty = graph.makeType().name(PropertyName.GEO_LOCATION.toString()).dataType(Geoshape.class).unique(Direction.OUT).indexed("search", Vertex.class).makePropertyKey();
        }
        properties.put(geoLocationProperty.getName(), geoLocationProperty);

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
        TitanKey rowKeyProperty = (TitanKey) graph.getType(PropertyName.ROW_KEY.toString());
        if (rowKeyProperty == null) {
            rowKeyProperty = graph.makeType().name(PropertyName.ROW_KEY.toString()).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(rowKeyProperty.getName(), rowKeyProperty);

        TitanKey sourceProperty = (TitanKey) graph.getType(PropertyName.SOURCE.toString());
        if (sourceProperty == null) {
            sourceProperty = graph.makeType().name(PropertyName.SOURCE.toString()).dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }
        properties.put(sourceProperty.getName(), sourceProperty);

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
