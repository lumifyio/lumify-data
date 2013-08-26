package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.cmdline.RedDawnCommandLineBase;
import com.altamiracorp.reddawn.model.TitanGraphVertex;
import com.altamiracorp.reddawn.model.ontology.LabelName;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.PropertyType;
import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanLabel;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.cli.CommandLine;

import java.util.*;

public abstract class Base extends RedDawnCommandLineBase {
    protected Map<String, TitanKey> properties = new HashMap<String, TitanKey>();
    protected Map<String, TitanLabel> edges = new HashMap<String, TitanLabel>();

    @Override
    protected int run(CommandLine cmd) throws Exception {
        // concept properties
        TitanGraph graph = (TitanGraph) createRedDawnSession().getGraphSession().getGraph();

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

        Iterator<Vertex> artifactIter = graph.getVertices(PropertyName.ONTOLOGY_TITLE.toString(), VertexType.ARTIFACT.toString()).iterator();
        TitanGraphVertex artifact;
        if (artifactIter.hasNext()) {
            artifact = new TitanGraphVertex(artifactIter.next());
        } else {
            artifact = new TitanGraphVertex(graph.addVertex(null));
            artifact.setProperty(typeProperty.getName(), VertexType.CONCEPT.toString());
            artifact.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), VertexType.ARTIFACT.toString());
        }
        addPropertyToConcept(graph, artifact, typeProperty.getName(), "Type", PropertyType.STRING);
        addPropertyToConcept(graph, artifact, subTypeProperty.getName(), "Subtype", PropertyType.STRING);
        addPropertyToConcept(graph, artifact, titleProperty.getName(), "Title", PropertyType.STRING);
        addPropertyToConcept(graph, artifact, geoLocationProperty.getName(), "Geo-location", PropertyType.GEO_LOCATION);

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
        Iterator<Vertex> entityIter = graph.getVertices(PropertyName.ONTOLOGY_TITLE.toString(), VertexType.ENTITY.toString()).iterator();
        TitanGraphVertex entity;
        if (entityIter.hasNext()) {
            entity = new TitanGraphVertex(entityIter.next());
        } else {
            entity = new TitanGraphVertex(graph.addVertex(null));
        }
        entity.setProperty(typeProperty.getName(), VertexType.CONCEPT.toString());
        entity.addProperty(PropertyName.ONTOLOGY_TITLE.toString(), VertexType.ENTITY.toString());

        addPropertyToConcept(graph, entity, typeProperty.getName(), "Type", PropertyType.STRING);
        addPropertyToConcept(graph, entity, subTypeProperty.getName(), "Subtype", PropertyType.STRING);
        addPropertyToConcept(graph, entity, titleProperty.getName(), "Title", PropertyType.STRING);
        addPropertyToConcept(graph, entity, PropertyName.GLYPH_ICON.toString(), "glyph icon", PropertyType.IMAGE);
        getOrCreateRelationshipType(graph, entity, artifact, LabelName.HAS_IMAGE.toString(), "has image");

        graph.commit();

        // Artifact to TermMention relationship
        getOrCreateRelationshipType(graph, artifact, entity, LabelName.HAS_ENTITY.toString(), "has entity");

        graph.commit();

        int returnCode = defineOntology(graph, entity);

        graph.commit();
        graph.shutdown();
        return returnCode;
    }

    protected abstract int defineOntology(TitanGraph graph, TitanGraphVertex entity);

    protected TitanGraphVertex getOrCreateConcept(TitanGraph graph, TitanGraphVertex parent, String conceptName) {
        Iterator<Vertex> iter = graph.getVertices(PropertyName.ONTOLOGY_TITLE.toString(), conceptName).iterator();
        TitanGraphVertex vertex;
        if (iter.hasNext()) {
            vertex = new TitanGraphVertex(iter.next());
        } else {
            vertex = new TitanGraphVertex(graph.addVertex(null));
        }
        vertex.setProperty(PropertyName.TYPE.toString(), VertexType.CONCEPT.toString());
        vertex.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), conceptName);
        findOrAddEdge(vertex, parent, LabelName.IS_A.toString());
        return vertex;
    }

    protected void getOrCreateRelationshipType(TitanGraph graph, TitanGraphVertex fromVertex, TitanGraphVertex toVertex, String relationshipName, String displayName) {
        TitanLabel relationshipLabel = (TitanLabel) graph.getType(relationshipName);
        if (relationshipLabel == null) {
            relationshipLabel = graph.makeType().name(relationshipName).directed().makeEdgeLabel();
            relationshipLabel.setProperty(PropertyName.TYPE.toString(), VertexType.RELATIONSHIP.toString());
            relationshipLabel.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), relationshipName);
            relationshipLabel.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
            graph.commit();
        }

        TitanLabel hasEdgeLabel = this.edges.get(LabelName.HAS_EDGE.toString());
        findOrAddEdge(fromVertex, new TitanGraphVertex(relationshipLabel), hasEdgeLabel);
        findOrAddEdge(new TitanGraphVertex(relationshipLabel), toVertex, hasEdgeLabel);
        graph.commit();
    }

    protected void findOrAddEdge(TitanGraphVertex fromVertex, TitanGraphVertex toVertex, TitanLabel edgeLabel) {
        findOrAddEdge(fromVertex, toVertex, edgeLabel.getName());
    }

    protected void findOrAddEdge(TitanGraphVertex fromVertex, TitanGraphVertex toVertex, String edgeLabel) {
        Iterator<Edge> possibleEdgeMatches = fromVertex.getVertex().getEdges(Direction.OUT, edgeLabel).iterator();
        while (possibleEdgeMatches.hasNext()) {
            Edge possibleEdgeMatch = possibleEdgeMatches.next();
            TitanGraphVertex possibleMatch = new TitanGraphVertex(possibleEdgeMatch.getVertex(Direction.IN));
            if (possibleMatch.getId().equals(toVertex.getId())) {
                return;
            }
        }
        fromVertex.getVertex().addEdge(edgeLabel, toVertex.getVertex());
    }

    protected TitanGraphVertex addPropertyToConcept(TitanGraph graph, TitanGraphVertex concept, String propertyName, String displayName, PropertyType dataType) {
        TitanKey typeProperty = (TitanKey) graph.getType(propertyName);
        if (typeProperty == null) {
            Class vertexDataType = String.class;
            switch (dataType) {
                case DATE:
                    vertexDataType = Date.class;
                    break;
                case CURRENCY:
                    vertexDataType = Double.class;
                    break;
                case IMAGE:
                case STRING:
                    vertexDataType = String.class;
                    break;
                case GEO_LOCATION:
                    vertexDataType = Geoshape.class;
                    break;
            }
            typeProperty = graph.makeType().name(propertyName).dataType(vertexDataType).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(typeProperty.getName(), typeProperty);

        Iterator<Vertex> iter = graph.getVertices(PropertyName.ONTOLOGY_TITLE.toString(), propertyName).iterator();
        TitanGraphVertex propertyVertex;
        if (iter.hasNext()) {
            propertyVertex = new TitanGraphVertex(iter.next());
        } else {
            propertyVertex = new TitanGraphVertex(graph.addVertex(null));
        }
        propertyVertex.setProperty(PropertyName.TYPE.toString(), VertexType.PROPERTY.toString());
        propertyVertex.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), propertyName);
        propertyVertex.setProperty(PropertyName.DATA_TYPE.toString(), dataType.toString());
        propertyVertex.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
        graph.commit();

        findOrAddEdge(concept, propertyVertex, LabelName.HAS_PROPERTY.toString());
        graph.commit();

        return propertyVertex;
    }

    protected List<String> generateColorPalette(int number) {
        List<String> palette = new ArrayList<String>(number);

        float hue = 0f;
        float inc = 1.0f / number;
        for (int i = 0; i < number; i++) {
            palette.add(toRGB(hue, 0.6f, 0.4f));
            hue += inc;
        }

        return palette;
    }

    public static String toRGB(float h, float s, float l) {
        float q = 0;

        if (l < 0.5)
            q = l * (1 + s);
        else
            q = (l + s) - (s * l);

        float p = 2 * l - q;

        float r = Math.max(0, HueToRGB(p, q, h + (1.0f / 3.0f)));
        float g = Math.max(0, HueToRGB(p, q, h));
        float b = Math.max(0, HueToRGB(p, q, h - (1.0f / 3.0f)));

        r = Math.min(r, 1.0f) * 255f;
        g = Math.min(g, 1.0f) * 255f;
        b = Math.min(b, 1.0f) * 255f;

        return "rgb(" + (int) r + ", " + (int) g + ", " + (int) b + ")";
    }

    private static float HueToRGB(float p, float q, float h) {
        if (h < 0) h += 1;

        if (h > 1) h -= 1;

        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }

        if (2 * h < 1) {
            return q;
        }

        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
        }

        return p;
    }

}
