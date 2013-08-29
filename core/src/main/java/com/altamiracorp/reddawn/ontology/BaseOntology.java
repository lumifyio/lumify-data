package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.ontology.*;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanLabel;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

public class BaseOntology {
    private final OntologyRepository ontologyRepository = new OntologyRepository();

    public void defineOntology(RedDawnSession redDawnSession) {
        // concept properties
        TitanGraph graph = (TitanGraph) redDawnSession.getGraphSession().getGraph();

        TitanKey typeProperty = (TitanKey) graph.getType(PropertyName.TYPE.toString());
        if (typeProperty == null) {
            typeProperty = graph.makeType().name(PropertyName.TYPE.toString()).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }

        TitanKey dataTypeProperty = (TitanKey) graph.getType(PropertyName.DATA_TYPE.toString());
        if (dataTypeProperty == null) {
            graph.makeType().name(PropertyName.DATA_TYPE.toString()).dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }

        TitanKey ontologyTitleProperty = (TitanKey) graph.getType(PropertyName.ONTOLOGY_TITLE.toString());
        if (ontologyTitleProperty == null) {
            graph.makeType().name(PropertyName.ONTOLOGY_TITLE.toString()).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }

        // concept edges
        TitanLabel hasPropertyEdge = (TitanLabel) graph.getType(LabelName.HAS_PROPERTY.toString());
        if (hasPropertyEdge == null) {
            graph.makeType().name(LabelName.HAS_PROPERTY.toString()).directed().makeEdgeLabel();
        }

        TitanLabel hasEdgeEdge = (TitanLabel) graph.getType(LabelName.HAS_EDGE.toString());
        if (hasEdgeEdge == null) {
            graph.makeType().name(LabelName.HAS_EDGE.toString()).directed().makeEdgeLabel();
        }

        TitanLabel isAEdge = (TitanLabel) graph.getType(LabelName.IS_A.toString());
        if (isAEdge == null) {
            graph.makeType().name(LabelName.IS_A.toString()).directed().makeEdgeLabel();
        }

        TitanKey relationshipType = (TitanKey) graph.getType(PropertyName.RELATIONSHIP_TYPE.toString());
        if (relationshipType == null) {
            graph.makeType().name(PropertyName.RELATIONSHIP_TYPE.toString()).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }

        TitanKey subTypeProperty = (TitanKey) graph.getType(PropertyName.SUBTYPE.toString());
        if (subTypeProperty == null) {
            subTypeProperty = graph.makeType().name(PropertyName.SUBTYPE.toString()).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }

        TitanKey displayNameProperty = (TitanKey) graph.getType(PropertyName.DISPLAY_NAME.toString());
        if (displayNameProperty == null) {
            graph.makeType().name(PropertyName.DISPLAY_NAME.toString()).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }

        TitanKey titleProperty = (TitanKey) graph.getType(PropertyName.TITLE.toString());
        if (titleProperty == null) {
            titleProperty = graph.makeType().name(PropertyName.TITLE.toString()).dataType(String.class).unique(Direction.OUT).indexed("search", Vertex.class).makePropertyKey();
        }

        TitanKey glyphIconProperty = (TitanKey) graph.getType(PropertyName.GLYPH_ICON.toString());
        if (glyphIconProperty == null) {
            graph.makeType().name(PropertyName.GLYPH_ICON.toString()).dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }

        TitanKey colorProperty = (TitanKey) graph.getType(PropertyName.COLOR.toString());
        if (colorProperty == null) {
            graph.makeType().name(PropertyName.COLOR.toString()).dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }

        TitanKey geoLocationProperty = (TitanKey) graph.getType(PropertyName.GEO_LOCATION.toString());
        if (geoLocationProperty == null) {
            geoLocationProperty = graph.makeType().name(PropertyName.GEO_LOCATION.toString()).dataType(Geoshape.class).unique(Direction.OUT).indexed("search", Vertex.class).makePropertyKey();
        }

        graph.commit();

        Concept rootConcept = ontologyRepository.getOrCreateConcept(redDawnSession.getGraphSession(), null, OntologyRepository.ROOT_CONCEPT_NAME, OntologyRepository.ROOT_CONCEPT_NAME);
        graph.commit();

        Concept artifact = ontologyRepository.getOrCreateConcept(redDawnSession.getGraphSession(), rootConcept, VertexType.ARTIFACT.toString(), "Artifact");
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), artifact, typeProperty.getName(), "Type", PropertyType.STRING);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), artifact, subTypeProperty.getName(), "Subtype", PropertyType.STRING);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), artifact, titleProperty.getName(), "Title", PropertyType.STRING);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), artifact, geoLocationProperty.getName(), "Geo-location", PropertyType.GEO_LOCATION);
        graph.commit();

        // TermMention concept
        TitanKey rowKeyProperty = (TitanKey) graph.getType(PropertyName.ROW_KEY.toString());
        if (rowKeyProperty == null) {
            graph.makeType().name(PropertyName.ROW_KEY.toString()).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }

        TitanKey sourceProperty = (TitanKey) graph.getType(PropertyName.SOURCE.toString());
        if (sourceProperty == null) {
            graph.makeType().name(PropertyName.SOURCE.toString()).dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }

        graph.commit();

        // Entity concept
        Concept entity = ontologyRepository.getOrCreateConcept(redDawnSession.getGraphSession(), rootConcept, VertexType.ENTITY.toString(), "Entity");
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), entity, typeProperty.getName(), "Type", PropertyType.STRING);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), entity, subTypeProperty.getName(), "Subtype", PropertyType.STRING);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), entity, titleProperty.getName(), "Title", PropertyType.STRING);
        ontologyRepository.addPropertyTo(redDawnSession.getGraphSession(), entity, PropertyName.GLYPH_ICON.toString(), "glyph icon", PropertyType.IMAGE);

        ontologyRepository.getOrCreateRelationshipType(redDawnSession.getGraphSession(), entity, artifact, LabelName.HAS_IMAGE.toString(), "has image");
        graph.commit();

        // Artifact to TermMention relationship
        ontologyRepository.getOrCreateRelationshipType(redDawnSession.getGraphSession(), artifact, entity, LabelName.HAS_ENTITY.toString(), "has entity");

        graph.commit();
    }

    public boolean isOntologyDefined(RedDawnSession session) {
        try {
            Concept concept = ontologyRepository.getConceptByName(session.getGraphSession(), VertexType.ARTIFACT.toString());
            return concept != null; // todo should check for more
        } catch (Exception e) {
            if (e.getMessage().contains(PropertyName.ONTOLOGY_TITLE.toString())) {
                return false;
            }
            throw new RuntimeException(e);
        }
    }
}
