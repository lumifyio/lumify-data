package com.altamiracorp.lumify.ontology;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.GraphSession;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.*;
import com.altamiracorp.lumify.model.resources.ResourceRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactType;
import com.google.inject.Inject;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanLabel;
import com.thinkaurelius.titan.core.TypeMaker;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Date;

public class BaseOntology {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseOntology.class);

    private final OntologyRepository ontologyRepository;
    private final ResourceRepository resourceRepository;
    private final GraphSession graphSession;

    @Inject
    public BaseOntology(OntologyRepository ontologyRepository, ResourceRepository resourceRepository, GraphSession graphSession) {
        this.ontologyRepository = ontologyRepository;
        this.resourceRepository = resourceRepository;
        this.graphSession = graphSession;
    }

    public void defineOntology(User user) {
        // concept properties
        TitanGraph graph = (TitanGraph) this.graphSession.getGraph();

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

        TitanKey geoLocationDescriptionProperty = (TitanKey) graph.getType(PropertyName.GEO_LOCATION_DESCRIPTION.toString());
        if (geoLocationDescriptionProperty == null) {
            geoLocationDescriptionProperty = graph.makeType().name(PropertyName.GEO_LOCATION_DESCRIPTION.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed(Vertex.class).makePropertyKey();
        }

        TitanKey publishedDateProperty = (TitanKey) graph.getType(PropertyName.PUBLISHED_DATE.toString());
        if (publishedDateProperty == null) {
            publishedDateProperty = graph.makeType().name(PropertyName.PUBLISHED_DATE.toString()).dataType(Long.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed(Vertex.class).makePropertyKey();
        }

        TitanKey sourceProperty = (TitanKey) graph.getType(PropertyName.SOURCE.toString());
        if (sourceProperty == null) {
            sourceProperty = graph.makeType().name(PropertyName.SOURCE.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed(Vertex.class).makePropertyKey();
        }

        graph.commit();

        Concept rootConcept = ontologyRepository.getOrCreateConcept(null, OntologyRepository.ROOT_CONCEPT_NAME, OntologyRepository.ROOT_CONCEPT_NAME, user);
        ontologyRepository.addPropertyTo(rootConcept, PropertyName.GLYPH_ICON.toString(), "glyph icon", PropertyType.IMAGE, user);
        ontologyRepository.addPropertyTo(rootConcept, PropertyName.MAP_GLYPH_ICON.toString(), "map glyph icon", PropertyType.IMAGE, user);
        graph.commit();

        Concept artifact = ontologyRepository.getOrCreateConcept(rootConcept, VertexType.ARTIFACT.toString(), "Artifact", user);
        ontologyRepository.addPropertyTo(artifact, typeProperty.getName(), "Type", PropertyType.STRING, user);
        ontologyRepository.addPropertyTo(artifact, subTypeProperty.getName(), "Subtype", PropertyType.STRING, user);
        ontologyRepository.addPropertyTo(artifact, titleProperty.getName(), "Title", PropertyType.STRING, user);
        ontologyRepository.addPropertyTo(artifact, geoLocationProperty.getName(), "Geo-location", PropertyType.GEO_LOCATION, user);
        ontologyRepository.addPropertyTo(artifact, geoLocationDescriptionProperty.getName(), "Geo-location Description", PropertyType.STRING, user);
        ontologyRepository.addPropertyTo(artifact, publishedDateProperty.getName(), "Published Date", PropertyType.DATE, user);
        ontologyRepository.addPropertyTo(artifact, sourceProperty.getName(), "Source", PropertyType.STRING, user);
        graph.commit();

        InputStream artifactGlyphIconInputStream = this.getClass().getResourceAsStream("artifact.png");
        String artifactGlyphIconRowKey = resourceRepository.importFile(artifactGlyphIconInputStream, "png", user);
        artifact.setProperty(PropertyName.GLYPH_ICON, artifactGlyphIconRowKey);
        graph.commit();

        ontologyRepository.getOrCreateConcept(artifact, ArtifactType.DOCUMENT.toString(), "Document", user);
        ontologyRepository.getOrCreateConcept(artifact, ArtifactType.VIDEO.toString(), "Video", user);

        Concept image = ontologyRepository.getOrCreateConcept(artifact, ArtifactType.IMAGE.toString(), "Image", user);

        // TermMention concept
        TitanKey rowKeyProperty = (TitanKey) graph.getType(PropertyName.ROW_KEY.toString());
        if (rowKeyProperty == null) {
            graph.makeType().name(PropertyName.ROW_KEY.toString()).dataType(String.class).unique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).indexed(Vertex.class).makePropertyKey();
        }

        graph.commit();

        // Entity concept
        Concept entity = ontologyRepository.getOrCreateConcept(rootConcept, VertexType.ENTITY.toString(), "Entity", user);
        ontologyRepository.addPropertyTo(entity, typeProperty.getName(), "Type", PropertyType.STRING, user);
        ontologyRepository.addPropertyTo(entity, subTypeProperty.getName(), "Subtype", PropertyType.STRING, user);
        ontologyRepository.addPropertyTo(entity, titleProperty.getName(), "Title", PropertyType.STRING, user);

        ontologyRepository.getOrCreateRelationshipType(entity, artifact, LabelName.HAS_IMAGE.toString(), "has image", user);

        graph.commit();

        InputStream entityGlyphIconInputStream = this.getClass().getResourceAsStream("entity.png");
        String entityGlyphIconRowKey = resourceRepository.importFile(entityGlyphIconInputStream, "png", user);
        entity.setProperty(PropertyName.GLYPH_ICON, entityGlyphIconRowKey);
        graph.commit();

        // Image to Entity relationship
        GraphVertex containsImageOf = ontologyRepository.getOrCreateRelationshipType(image, entity, LabelName.CONTAINS_IMAGE_OF.toString(), "contains image of", user);
        ontologyRepository.addPropertyTo(containsImageOf, PropertyName.BOUNDING_BOX.toString(), "Bounding Box", PropertyType.STRING, user);
        graph.commit();

        // Artifact to TermMention relationship
        ontologyRepository.getOrCreateRelationshipType(artifact, entity, LabelName.HAS_ENTITY.toString(), "has entity", user);
        graph.commit();
    }

    public boolean isOntologyDefined(User user) {
        try {
            Concept concept = ontologyRepository.getConceptByName(VertexType.ARTIFACT.toString(), user);
            return concept != null; // todo should check for more
        } catch (Exception e) {
            if (e.getMessage().contains(PropertyName.ONTOLOGY_TITLE.toString())) {
                return false;
            }
            throw new RuntimeException(e);
        }
    }

    public void initialize(User user) {
        if (!isOntologyDefined(user)) {
            LOGGER.info("Base ontology not defined. Creating a new ontology.");
            defineOntology(user);
        } else {
            LOGGER.info("Base ontology already defined.");
        }
    }
}
