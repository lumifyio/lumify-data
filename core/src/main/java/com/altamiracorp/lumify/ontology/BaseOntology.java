package com.altamiracorp.lumify.ontology;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.*;
import com.altamiracorp.lumify.model.resources.ResourceRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactType;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanLabel;
import com.thinkaurelius.titan.core.TypeMaker;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

import java.io.InputStream;
import java.util.Date;

public class BaseOntology {
    private final OntologyRepository ontologyRepository = new OntologyRepository();
    private final ResourceRepository resourceRepository = new ResourceRepository();

    public void defineOntology(AppSession session) {
        // concept properties
        TitanGraph graph = (TitanGraph) session.getGraphSession().getGraph();

        TitanKey typeProperty = (TitanKey) graph.getType(PropertyName.TYPE.toString());
        if (typeProperty == null) {
            typeProperty = graph.makeType().name(PropertyName.TYPE.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed(Vertex.class).makePropertyKey();
        }

        TitanKey dataTypeProperty = (TitanKey) graph.getType(PropertyName.DATA_TYPE.toString());
        if (dataTypeProperty == null) {
            graph.makeType().name(PropertyName.DATA_TYPE.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).makePropertyKey();
        }

        TitanKey ontologyTitleProperty = (TitanKey) graph.getType(PropertyName.ONTOLOGY_TITLE.toString());
        if (ontologyTitleProperty == null) {
            graph.makeType().name(PropertyName.ONTOLOGY_TITLE.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed(Vertex.class).makePropertyKey();
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
            graph.makeType().name(PropertyName.RELATIONSHIP_TYPE.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed(Vertex.class).makePropertyKey();
        }

        TitanKey timeStampProperty = (TitanKey) graph.getType(PropertyName.TIME_STAMP.toString());
        if (timeStampProperty == null) {
            graph.makeType().name(PropertyName.TIME_STAMP.toString()).dataType(Date.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).makePropertyKey();
        }

        TitanKey subTypeProperty = (TitanKey) graph.getType(PropertyName.SUBTYPE.toString());
        if (subTypeProperty == null) {
            subTypeProperty = graph.makeType().name(PropertyName.SUBTYPE.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed(Vertex.class).makePropertyKey();
        }

        TitanKey displayNameProperty = (TitanKey) graph.getType(PropertyName.DISPLAY_NAME.toString());
        if (displayNameProperty == null) {
            graph.makeType().name(PropertyName.DISPLAY_NAME.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed(Vertex.class).makePropertyKey();
        }

        TitanKey titleProperty = (TitanKey) graph.getType(PropertyName.TITLE.toString());
        if (titleProperty == null) {
            titleProperty = graph.makeType().name(PropertyName.TITLE.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed("search", Vertex.class).makePropertyKey();
        }

        TitanKey glyphIconProperty = (TitanKey) graph.getType(PropertyName.GLYPH_ICON.toString());
        if (glyphIconProperty == null) {
            graph.makeType().name(PropertyName.GLYPH_ICON.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).makePropertyKey();
        }

        TitanKey mapGlyphIconProperty = (TitanKey) graph.getType(PropertyName.MAP_GLYPH_ICON.toString());
        if (mapGlyphIconProperty == null) {
            graph.makeType().name(PropertyName.MAP_GLYPH_ICON.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).makePropertyKey();
        }

        TitanKey colorProperty = (TitanKey) graph.getType(PropertyName.COLOR.toString());
        if (colorProperty == null) {
            graph.makeType().name(PropertyName.COLOR.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).makePropertyKey();
        }

        TitanKey geoLocationProperty = (TitanKey) graph.getType(PropertyName.GEO_LOCATION.toString());
        if (geoLocationProperty == null) {
            geoLocationProperty = graph.makeType().name(PropertyName.GEO_LOCATION.toString()).dataType(Geoshape.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed("search", Vertex.class).makePropertyKey();
        }

        TitanKey publishedDateProperty = (TitanKey) graph.getType(PropertyName.PUBLISHED_DATE.toString());
        if (publishedDateProperty == null) {
            graph.makeType().name(PropertyName.PUBLISHED_DATE.toString()).dataType(Long.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed(Vertex.class).makePropertyKey();
        }

        TitanKey sourceProperty = (TitanKey) graph.getType(PropertyName.SOURCE.toString());
        if (sourceProperty == null) {
            graph.makeType().name(PropertyName.SOURCE.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed(Vertex.class).makePropertyKey();
        }

        graph.commit();

        Concept rootConcept = ontologyRepository.getOrCreateConcept(session.getGraphSession(), null, OntologyRepository.ROOT_CONCEPT_NAME, OntologyRepository.ROOT_CONCEPT_NAME);
        ontologyRepository.addPropertyTo(session.getGraphSession(), rootConcept, PropertyName.GLYPH_ICON.toString(), "glyph icon", PropertyType.IMAGE);
        ontologyRepository.addPropertyTo(session.getGraphSession(), rootConcept, PropertyName.MAP_GLYPH_ICON.toString(), "map glyph icon", PropertyType.IMAGE);
        graph.commit();

        Concept artifact = ontologyRepository.getOrCreateConcept(session.getGraphSession(), rootConcept, VertexType.ARTIFACT.toString(), "Artifact");
        ontologyRepository.addPropertyTo(session.getGraphSession(), artifact, typeProperty.getName(), "Type", PropertyType.STRING);
        ontologyRepository.addPropertyTo(session.getGraphSession(), artifact, subTypeProperty.getName(), "Subtype", PropertyType.STRING);
        ontologyRepository.addPropertyTo(session.getGraphSession(), artifact, titleProperty.getName(), "Title", PropertyType.STRING);
        ontologyRepository.addPropertyTo(session.getGraphSession(), artifact, geoLocationProperty.getName(), "Geo-location", PropertyType.GEO_LOCATION);
        ontologyRepository.addPropertyTo(session.getGraphSession(), artifact, publishedDateProperty.getName(), "Published Date", PropertyType.DATE);
        graph.commit();

        InputStream artifactGlyphIconInputStream = this.getClass().getResourceAsStream("artifact.png");
        String artifactGlyphIconRowKey = resourceRepository.importFile(session.getModelSession(), artifactGlyphIconInputStream, "png");
        artifact.setProperty(PropertyName.GLYPH_ICON, artifactGlyphIconRowKey);
        graph.commit();

        ontologyRepository.getOrCreateConcept(session.getGraphSession(), artifact, ArtifactType.DOCUMENT.toString(), "Document");
        ontologyRepository.getOrCreateConcept(session.getGraphSession(), artifact, ArtifactType.VIDEO.toString(), "Video");

        Concept image = ontologyRepository.getOrCreateConcept(session.getGraphSession(), artifact, ArtifactType.IMAGE.toString(), "Image");

        // TermMention concept
        TitanKey rowKeyProperty = (TitanKey) graph.getType(PropertyName.ROW_KEY.toString());
        if (rowKeyProperty == null) {
            graph.makeType().name(PropertyName.ROW_KEY.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed(Vertex.class).makePropertyKey();
        }

        graph.commit();

        // Entity concept
        Concept entity = ontologyRepository.getOrCreateConcept(session.getGraphSession(), rootConcept, VertexType.ENTITY.toString(), "Entity");
        ontologyRepository.addPropertyTo(session.getGraphSession(), entity, typeProperty.getName(), "Type", PropertyType.STRING);
        ontologyRepository.addPropertyTo(session.getGraphSession(), entity, subTypeProperty.getName(), "Subtype", PropertyType.STRING);
        ontologyRepository.addPropertyTo(session.getGraphSession(), entity, titleProperty.getName(), "Title", PropertyType.STRING);

        ontologyRepository.getOrCreateRelationshipType(session.getGraphSession(), entity, artifact, LabelName.HAS_IMAGE.toString(), "has image");

        graph.commit();

        InputStream entityGlyphIconInputStream = this.getClass().getResourceAsStream("entity.png");
        String entityGlyphIconRowKey = resourceRepository.importFile(session.getModelSession(), entityGlyphIconInputStream, "png");
        entity.setProperty(PropertyName.GLYPH_ICON, entityGlyphIconRowKey);
        graph.commit();

        // Image to Entity relationship
        GraphVertex containsImageOf = ontologyRepository.getOrCreateRelationshipType(session.getGraphSession(), image, entity, LabelName.CONTAINS_IMAGE_OF.toString(), "contains image of");
        ontologyRepository.addPropertyTo(session.getGraphSession(), containsImageOf, PropertyName.BOUNDING_BOX.toString(), "Bounding Box", PropertyType.STRING);
        graph.commit();

        // Artifact to TermMention relationship
        ontologyRepository.getOrCreateRelationshipType(session.getGraphSession(), artifact, entity, LabelName.HAS_ENTITY.toString(), "has entity");
        graph.commit();
    }

    public boolean isOntologyDefined(AppSession session) {
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
