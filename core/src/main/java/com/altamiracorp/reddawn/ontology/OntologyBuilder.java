package com.altamiracorp.reddawn.ontology;


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

import java.util.*;

public class OntologyBuilder {

    private final TitanGraph graph;

    public OntologyBuilder(TitanGraph graph) {
        this.graph = graph;
    }

    public TitanGraphVertex getOrCreateConcept(TitanGraphVertex parent, String conceptName, String displayName) {
        Iterator<Vertex> iter = graph.getVertices(PropertyName.ONTOLOGY_TITLE.toString(), conceptName).iterator();
        TitanGraphVertex vertex;
        if (iter.hasNext()) {
            vertex = new TitanGraphVertex(iter.next());
        } else {
            vertex = new TitanGraphVertex(graph.addVertex(null));
        }
        vertex.setProperty(PropertyName.TYPE.toString(), VertexType.CONCEPT.toString());
        vertex.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), conceptName);
        vertex.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
        if (parent != null) {
            findOrAddEdge(vertex, parent, LabelName.IS_A.toString());
        }
        return vertex;
    }

    public TitanGraphVertex getRelationship(String relationshipLabel, String displayName) {
        Iterator<Vertex> iter = graph.getVertices(PropertyName.ONTOLOGY_TITLE.toString(), relationshipLabel).iterator();
        TitanGraphVertex vertex;
        if (iter.hasNext()) {
            vertex = new TitanGraphVertex(iter.next());
        } else {
            vertex = new TitanGraphVertex(graph.addVertex(null));
        }
        vertex.setProperty(PropertyName.TYPE.toString(), VertexType.RELATIONSHIP.toString());
        vertex.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), relationshipLabel);
        vertex.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
        return vertex;
    }

    public void getOrCreateRelationshipType(TitanGraphVertex fromVertex, TitanGraphVertex toVertex, String relationshipName, String displayName, Map<String, TitanLabel> edges) {
        TitanLabel relationshipLabel = (TitanLabel) graph.getType(relationshipName);
        if (relationshipLabel == null) {
            relationshipLabel = graph.makeType().name(relationshipName).directed().makeEdgeLabel();
            relationshipLabel.setProperty(PropertyName.TYPE.toString(), VertexType.RELATIONSHIP.toString());
            relationshipLabel.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), relationshipName);
            relationshipLabel.setProperty(PropertyName.DISPLAY_NAME.toString(), displayName);
            graph.commit();
        }

        TitanLabel hasEdgeLabel = edges.get(LabelName.HAS_EDGE.toString());
        findOrAddEdge(fromVertex, new TitanGraphVertex(relationshipLabel), hasEdgeLabel);
        findOrAddEdge(new TitanGraphVertex(relationshipLabel), toVertex, hasEdgeLabel);
        graph.commit();
    }

    public void findOrAddEdge(TitanGraphVertex fromVertex, TitanGraphVertex toVertex, TitanLabel edgeLabel) {
        findOrAddEdge(fromVertex, toVertex, edgeLabel.getName());
    }

    public void findOrAddEdge(TitanGraphVertex fromVertex, TitanGraphVertex toVertex, String edgeLabel) {
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

    public TitanGraphVertex addPropertyTo(TitanGraphVertex vertex, String propertyName, String displayName, PropertyType dataType, Map<String, TitanKey> properties) {
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

        findOrAddEdge(vertex, propertyVertex, LabelName.HAS_PROPERTY.toString());
        graph.commit();

        return propertyVertex;
    }

    public TitanGraphVertex addPropertyTo(String relationshipLabel, String propertyName, String displayName, PropertyType dataType, Map<String, TitanKey> properties) {
        TitanGraphVertex vertex = getRelationship(relationshipLabel, displayName);
        return addPropertyTo(vertex, propertyName, displayName, dataType, properties);
    }

    public List<String> generateColorPalette(int number) {
        List<String> palette = new ArrayList<String>(number);

        float hue = 0f;
        float inc = 1.0f / number;
        for (int i = 0; i < number; i++) {
            palette.add(toRGB(hue, 0.6f, 0.4f));
            hue += inc;
        }

        return palette;
    }

    private static String toRGB(float h, float s, float l) {
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

    TitanKey getOrCreateTitanKey(PropertyName type, Map<String, TitanKey> properties) {
        return getOrCreateTitanKey(type, properties, String.class);
    }

    TitanKey getOrCreateTitanKey(PropertyName type, Map<String, TitanKey> properties, Class<?> clazz) {
        TitanKey typeProperty = (TitanKey) graph.getType(type.toString());
        if (typeProperty == null) {
            typeProperty = graph.makeType().name(type.toString()).dataType(clazz).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey();
        }
        properties.put(typeProperty.getName(), typeProperty);
        return typeProperty;
    }

    TitanLabel createConceptEdge(LabelName hasProperty, Map<String, TitanLabel> edges) {
        TitanLabel hasPropertyEdge = (TitanLabel) graph.getType(hasProperty.toString());
        if (hasPropertyEdge == null) {
            hasPropertyEdge = graph.makeType().name(hasProperty.toString()).directed().makeEdgeLabel();
        }
        edges.put(hasPropertyEdge.getName(), hasPropertyEdge);
        return hasPropertyEdge;
    }
}
