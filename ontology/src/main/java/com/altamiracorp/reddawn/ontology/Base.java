package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.cmdline.RedDawnCommandLineBase;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanLabel;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.cli.CommandLine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Base extends RedDawnCommandLineBase {
    protected Map<String, TitanKey> properties = new HashMap<String, TitanKey>();
    protected Map<String, TitanLabel> edges = new HashMap<String, TitanLabel>();

    @Override
    protected int run(CommandLine cmd) throws Exception {
        // concept properties
        TitanGraph graph = (TitanGraph) createRedDawnSession().getGraphSession().getGraph();

        TitanKey typeProperty = (TitanKey) graph.getType(OntologyRepository.TYPE_PROPERTY_NAME);
        if (typeProperty == null) {
            typeProperty = graph.makeType().name(OntologyRepository.TYPE_PROPERTY_NAME).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(typeProperty.getName(), typeProperty);

        TitanKey dataTypeProperty = (TitanKey) graph.getType(OntologyRepository.DATA_TYPE_PROPERTY_NAME);
        if (dataTypeProperty == null) {
            dataTypeProperty = graph.makeType().name(OntologyRepository.DATA_TYPE_PROPERTY_NAME).dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }
        properties.put(dataTypeProperty.getName(), dataTypeProperty);

        TitanKey ontologyTitleProperty = (TitanKey) graph.getType(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME);
        if (ontologyTitleProperty == null) {
            ontologyTitleProperty = graph.makeType().name(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(ontologyTitleProperty.getName(), ontologyTitleProperty);

        // concept edges
        TitanLabel hasPropertyEdge = (TitanLabel) graph.getType(OntologyRepository.HAS_PROPERTY_LABEL_NAME);
        if (hasPropertyEdge == null) {
            hasPropertyEdge = graph.makeType().name(OntologyRepository.HAS_PROPERTY_LABEL_NAME).directed().makeEdgeLabel();
        }
        edges.put(hasPropertyEdge.getName(), hasPropertyEdge);

        TitanLabel hasEdgeEdge = (TitanLabel) graph.getType(OntologyRepository.HAS_EDGE_LABEL_NAME);
        if (hasEdgeEdge == null) {
            hasEdgeEdge = graph.makeType().name(OntologyRepository.HAS_EDGE_LABEL_NAME).directed().makeEdgeLabel();
        }
        edges.put(hasEdgeEdge.getName(), hasEdgeEdge);

        TitanLabel isAEdge = (TitanLabel) graph.getType(OntologyRepository.IS_A_LABEL_NAME);
        if (isAEdge == null) {
            isAEdge = graph.makeType().name(OntologyRepository.IS_A_LABEL_NAME).directed().makeEdgeLabel();
        }
        edges.put(isAEdge.getName(), isAEdge);

        // Artifact concept
        TitanKey subTypeProperty = (TitanKey) graph.getType(OntologyRepository.SUBTYPE_PROPERTY_NAME);
        if (subTypeProperty == null) {
            subTypeProperty = graph.makeType().name(OntologyRepository.SUBTYPE_PROPERTY_NAME).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(subTypeProperty.getName(), subTypeProperty);

        TitanKey titleProperty = (TitanKey) graph.getType(OntologyRepository.TITLE_PROPERTY_NAME);
        if (titleProperty == null) {
            titleProperty = graph.makeType().name(OntologyRepository.TITLE_PROPERTY_NAME).dataType(String.class).unique(Direction.OUT).indexed("search", Vertex.class).makePropertyKey();
        }
        properties.put(titleProperty.getName(), titleProperty);

        TitanKey glyphIconProperty = (TitanKey) graph.getType(OntologyRepository.GLYPH_ICON_PROPERTY_NAME);
        if (glyphIconProperty == null) {
            glyphIconProperty = graph.makeType().name(OntologyRepository.GLYPH_ICON_PROPERTY_NAME).dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }
        properties.put(glyphIconProperty.getName(), glyphIconProperty);

        TitanKey colorProperty = (TitanKey) graph.getType(OntologyRepository.COLOR_PROPERTY_NAME);
        if (colorProperty == null) {
            colorProperty = graph.makeType().name(OntologyRepository.COLOR_PROPERTY_NAME).dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }
        properties.put(colorProperty.getName(), colorProperty);

        TitanKey geoLocationProperty = (TitanKey) graph.getType(OntologyRepository.GEO_LOCATION_PROPERTY_NAME);
        if (geoLocationProperty == null) {
            geoLocationProperty = graph.makeType().name(OntologyRepository.GEO_LOCATION_PROPERTY_NAME).dataType(Geoshape.class).unique(Direction.OUT).indexed("search", Vertex.class).makePropertyKey();
        }
        properties.put(geoLocationProperty.getName(), geoLocationProperty);

        graph.commit();

        Iterator<Vertex> artifactIter = graph.getVertices(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME, OntologyRepository.ARTIFACT_TYPE).iterator();
        TitanVertex artifact;
        if (artifactIter.hasNext()) {
            artifact = (TitanVertex) artifactIter.next();
        } else {
            artifact = (TitanVertex) graph.addVertex(null);
            artifact.setProperty(typeProperty.getName(), OntologyRepository.CONCEPT_TYPE);
            artifact.setProperty(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME, OntologyRepository.ARTIFACT_TYPE);
        }
        addPropertyToConcept(graph, artifact, typeProperty.getName(), OntologyRepository.STRING_PROPERTY_TYPE);
        addPropertyToConcept(graph, artifact, subTypeProperty.getName(), OntologyRepository.STRING_PROPERTY_TYPE);
        addPropertyToConcept(graph, artifact, titleProperty.getName(), OntologyRepository.STRING_PROPERTY_TYPE);
        addPropertyToConcept(graph, artifact, geoLocationProperty.getName(), OntologyRepository.GEO_LOCATION_PROPERTY_TYPE);

        graph.commit();

        // TermMention concept
        TitanKey rowKeyProperty = (TitanKey) graph.getType(OntologyRepository.ROW_KEY_PROPERTY_NAME);
        if (rowKeyProperty == null) {
            rowKeyProperty = graph.makeType().name(OntologyRepository.ROW_KEY_PROPERTY_NAME).dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(rowKeyProperty.getName(), rowKeyProperty);

        TitanKey columnFamilyNameProperty = (TitanKey) graph.getType(OntologyRepository.COLUMN_FAMILY_NAME_PROPERTY_NAME);
        if (columnFamilyNameProperty == null) {
            columnFamilyNameProperty = graph.makeType().name(OntologyRepository.COLUMN_FAMILY_NAME_PROPERTY_NAME).dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }
        properties.put(columnFamilyNameProperty.getName(), columnFamilyNameProperty);

        graph.commit();

        Iterator<Vertex> termMentionIter = graph.getVertices(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME, OntologyRepository.TERM_MENTION_TYPE).iterator();
        TitanVertex termMention;
        if (termMentionIter.hasNext()) {
            termMention = (TitanVertex) termMentionIter.next();
        } else {
            termMention = (TitanVertex) graph.addVertex(null);
            termMention.setProperty(typeProperty.getName(), OntologyRepository.CONCEPT_TYPE);
            termMention.addProperty(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME, OntologyRepository.TERM_MENTION_TYPE);
        }
        addPropertyToConcept(graph, termMention, typeProperty.getName(), OntologyRepository.STRING_PROPERTY_TYPE);
        addPropertyToConcept(graph, termMention, subTypeProperty.getName(), OntologyRepository.STRING_PROPERTY_TYPE);
        addPropertyToConcept(graph, termMention, rowKeyProperty.getName(), OntologyRepository.STRING_PROPERTY_TYPE);
        addPropertyToConcept(graph, termMention, columnFamilyNameProperty.getName(), OntologyRepository.STRING_PROPERTY_TYPE);
        addPropertyToConcept(graph, termMention, titleProperty.getName(), OntologyRepository.STRING_PROPERTY_TYPE);

        graph.commit();

        // Entity concept
        Iterator<Vertex> entityIter = graph.getVertices(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME, OntologyRepository.ENTITY_TYPE).iterator();
        TitanVertex entity;
        if (entityIter.hasNext()) {
            entity = (TitanVertex) entityIter.next();
        } else {
            entity = (TitanVertex) graph.addVertex(null);
            entity.setProperty(typeProperty.getName(), OntologyRepository.CONCEPT_TYPE);
            entity.addProperty(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME, OntologyRepository.ENTITY_TYPE);
        }
        addPropertyToConcept(graph, entity, typeProperty.getName(), OntologyRepository.STRING_PROPERTY_TYPE);
        addPropertyToConcept(graph, entity, subTypeProperty.getName(), OntologyRepository.STRING_PROPERTY_TYPE);
        addPropertyToConcept(graph, entity, titleProperty.getName(), OntologyRepository.STRING_PROPERTY_TYPE);

        graph.commit();

        // Artifact to TermMention relationship
        TitanLabel hasTermMention = (TitanLabel) graph.getType(OntologyRepository.HAS_TERM_MENTION_LABEL_NAME);
        if (hasTermMention == null) {
            hasTermMention = graph.makeType().name(OntologyRepository.HAS_TERM_MENTION_LABEL_NAME).directed().makeEdgeLabel();
        }
        edges.put(hasTermMention.getName(), hasTermMention);
        artifact.addEdge(hasEdgeEdge, hasTermMention);
        findOrAddEdge(hasTermMention, termMention, hasEdgeEdge);

        graph.commit();

        int returnCode = defineOntology(graph, entity);

        graph.commit();
        graph.shutdown();
        return returnCode;
    }

    protected abstract int defineOntology(TitanGraph graph, TitanVertex entity);

    protected TitanVertex getOrCreateConcept(TitanGraph graph, TitanVertex parent, String conceptName) {
        Iterator<Vertex> iter = graph.getVertices(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME, conceptName).iterator();
        TitanVertex vertex;
        if (iter.hasNext()) {
            vertex = (TitanVertex) iter.next();
        } else {
            vertex = (TitanVertex) graph.addVertex(null);
            vertex.setProperty(OntologyRepository.TYPE_PROPERTY_NAME, OntologyRepository.CONCEPT_TYPE);
            vertex.setProperty(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME, conceptName);
        }
        findOrAddEdge(vertex, parent, OntologyRepository.IS_A_LABEL_NAME);
        return vertex;
    }

    protected void getOrCreateRelationshipType(TitanGraph graph, TitanVertex fromVertex, TitanVertex toVertex, String relationshipName) {
        TitanLabel relationshipLabel = (TitanLabel) graph.getType(relationshipName);
        if (relationshipLabel == null) {
            relationshipLabel = graph.makeType().name(relationshipName).directed().makeEdgeLabel();
            relationshipLabel.setProperty(OntologyRepository.TYPE_PROPERTY_NAME, OntologyRepository.RELATIONSHIP_TYPE);
            relationshipLabel.setProperty(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME, relationshipName);
        }

        TitanLabel hasEdgeLabel = this.edges.get(OntologyRepository.HAS_EDGE_LABEL_NAME);
        findOrAddEdge(fromVertex, relationshipLabel, hasEdgeLabel);
        findOrAddEdge(relationshipLabel, toVertex, hasEdgeLabel);
    }

    protected void findOrAddEdge(TitanVertex fromVertex, TitanVertex toVertex, TitanLabel edgeLabel) {
        findOrAddEdge(fromVertex, toVertex, edgeLabel.getName());
    }

    protected void findOrAddEdge(TitanVertex fromVertex, TitanVertex toVertex, String edgeLabel) {
        Iterator<Edge> possibleEdgeMatches = fromVertex.getEdges(Direction.OUT, edgeLabel).iterator();
        while (possibleEdgeMatches.hasNext()) {
            Edge possibleEdgeMatch = possibleEdgeMatches.next();
            Vertex possibleMatch = possibleEdgeMatch.getVertex(Direction.IN);
            if (possibleMatch.getId().equals(toVertex.getId())) {
                return;
            }
        }
        fromVertex.addEdge(edgeLabel, toVertex);
    }

    protected TitanVertex addPropertyToConcept(TitanGraph graph, TitanVertex concept, String propertyName, String dataType) {
        Iterator<Vertex> iter = graph.getVertices(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME, propertyName).iterator();
        TitanVertex propertyVertex;
        if (iter.hasNext()) {
            propertyVertex = (TitanVertex) iter.next();
        } else {
            propertyVertex = (TitanVertex) graph.addVertex(null);
            propertyVertex.setProperty(OntologyRepository.TYPE_PROPERTY_NAME, OntologyRepository.PROPERTY_TYPE);
            propertyVertex.setProperty(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME, propertyName);
            propertyVertex.setProperty(OntologyRepository.DATA_TYPE_PROPERTY_NAME, dataType);
        }
        findOrAddEdge(concept, propertyVertex, OntologyRepository.HAS_PROPERTY_LABEL_NAME);
        return propertyVertex;
    }
}
