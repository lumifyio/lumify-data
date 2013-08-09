package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.cmdline.RedDawnCommandLineBase;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanLabel;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.cli.CommandLine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Base extends RedDawnCommandLineBase {
    public static final String CONCEPT_TYPE = "Concept";
    public static final String ARTIFACT_TYPE = "Artifact";
    public static final String TERM_MENTION_TYPE = "TermMention";
    public static final String ENTITY_TYPE = "Entity";

    protected Map<String, TitanKey> properties = new HashMap<String, TitanKey>();
    protected Map<String, TitanLabel> edges = new HashMap<String, TitanLabel>();

    @Override
    protected int run(CommandLine cmd) throws Exception {
        // concept properties
        TitanGraph graph = (TitanGraph) createRedDawnSession().getGraphSession().getGraph();

        TitanKey typeProperty = (TitanKey) graph.getType("type");
        if (typeProperty == null) {
            typeProperty = graph.makeType().name("type").dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(typeProperty.getName(), typeProperty);

        TitanKey conceptProperty = (TitanKey) graph.getType("concept");
        if (conceptProperty == null) {
            conceptProperty = graph.makeType().name("concept").dataType(String.class).unique(Direction.IN).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(conceptProperty.getName(), conceptProperty);

        // concept edges
        TitanLabel hasPropertyEdge = (TitanLabel) graph.getType("hasProperty");
        if (hasPropertyEdge == null) {
            hasPropertyEdge = graph.makeType().name("hasProperty").directed().makeEdgeLabel();
        }
        edges.put(hasPropertyEdge.getName(), hasPropertyEdge);

        TitanLabel hasEdgeEdge = (TitanLabel) graph.getType("hasEdge");
        if (hasEdgeEdge == null) {
            hasEdgeEdge = graph.makeType().name("hasEdge").directed().makeEdgeLabel();
        }
        edges.put(hasEdgeEdge.getName(), hasEdgeEdge);

        // Artifact concept
        TitanKey subTypeProperty = (TitanKey) graph.getType("subType");
        if (subTypeProperty == null) {
            subTypeProperty = graph.makeType().name("subType").dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(subTypeProperty.getName(), subTypeProperty);

        TitanKey titleProperty = (TitanKey) graph.getType("title");
        if (titleProperty == null) {
            titleProperty = graph.makeType().name("title").dataType(String.class).unique(Direction.OUT).indexed("search", Vertex.class).makePropertyKey();
        }
        properties.put(titleProperty.getName(), titleProperty);

        TitanKey geoLocationProperty = (TitanKey) graph.getType("geoLocation");
        if (geoLocationProperty == null) {
            geoLocationProperty = graph.makeType().name("geoLocation").dataType(Geoshape.class).unique(Direction.OUT).indexed("search", Vertex.class).makePropertyKey();
        }
        properties.put(geoLocationProperty.getName(), geoLocationProperty);

        Iterator<Vertex> artifactIter = graph.getVertices(conceptProperty.getName(), ARTIFACT_TYPE).iterator();
        TitanVertex artifact;
        if (artifactIter.hasNext()) {
            artifact = (TitanVertex) artifactIter.next();
        } else {
            artifact = (TitanVertex) graph.addVertex(null);
            artifact.setProperty(typeProperty.getName(), CONCEPT_TYPE);
            artifact.addProperty(conceptProperty, ARTIFACT_TYPE);
            artifact.addEdge(hasPropertyEdge, typeProperty);
            artifact.addEdge(hasPropertyEdge, subTypeProperty);
            artifact.addEdge(hasPropertyEdge, titleProperty);
            artifact.addEdge(hasPropertyEdge, geoLocationProperty);
        }

        // TermMention concept
        TitanKey rowKeyProperty = (TitanKey) graph.getType("_rowKey");
        if (rowKeyProperty == null) {
            rowKeyProperty = graph.makeType().name("_rowKey").dataType(String.class).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(rowKeyProperty.getName(), rowKeyProperty);

        TitanKey columnFamilyNameProperty = (TitanKey) graph.getType("_columnFamilyName");
        if (columnFamilyNameProperty == null) {
            columnFamilyNameProperty = graph.makeType().name("_columnFamilyName").dataType(String.class).unique(Direction.OUT).makePropertyKey();
        }
        properties.put(columnFamilyNameProperty.getName(), columnFamilyNameProperty);

        Iterator<Vertex> termMentionIter = graph.getVertices(conceptProperty.getName(), TERM_MENTION_TYPE).iterator();
        TitanVertex termMention;
        if (termMentionIter.hasNext()) {
            termMention = (TitanVertex) termMentionIter.next();
        } else {
            termMention = (TitanVertex) graph.addVertex(null);
            termMention.setProperty(typeProperty.getName(), CONCEPT_TYPE);
            termMention.addProperty(conceptProperty, TERM_MENTION_TYPE);
            termMention.addEdge(hasPropertyEdge, typeProperty);
            termMention.addEdge(hasPropertyEdge, subTypeProperty);
            termMention.addEdge(hasPropertyEdge, rowKeyProperty);
            termMention.addEdge(hasPropertyEdge, columnFamilyNameProperty);
            termMention.addEdge(hasPropertyEdge, titleProperty);
        }

        // Entity concept
        Iterator<Vertex> entityIter = graph.getVertices(conceptProperty.getName(), ENTITY_TYPE).iterator();
        if (!entityIter.hasNext()) {
            TitanVertex entity = (TitanVertex) graph.addVertex(null);
            entity.setProperty(typeProperty.getName(), CONCEPT_TYPE);
            entity.addProperty(conceptProperty, ENTITY_TYPE);
            entity.addEdge(hasPropertyEdge, typeProperty);
            entity.addEdge(hasPropertyEdge, subTypeProperty);
            entity.addEdge(hasPropertyEdge, titleProperty);
        }

        // Artifact to TermMention relationship
        TitanLabel hasTermMention = (TitanLabel) graph.getType("hasTermMention");
        if (hasTermMention == null) {
            hasTermMention = graph.makeType().name("hasTermMention").directed().makeEdgeLabel();
        }
        edges.put(hasTermMention.getName(), hasTermMention);
        artifact.addEdge(hasEdgeEdge, hasTermMention);
        hasTermMention.addEdge(hasEdgeEdge, termMention);


        int returnCode = defineOntology(graph);

        graph.commit();
        graph.shutdown();
        return returnCode;
    }

    protected abstract int defineOntology(TitanGraph graph);
}
