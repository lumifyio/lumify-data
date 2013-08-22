package com.altamiracorp.reddawn.model.ontology;

import com.tinkerpop.blueprints.Vertex;

public class VertexProperty extends Property {
    private final Vertex vertex;

    public VertexProperty(Vertex vertex) {
        this.vertex = vertex;
    }

    @Override
    public String getId() {
        return getVertex().getId().toString();
    }

    @Override
    public String getTitle() {
        return getVertex().getProperty(PropertyName.ONTOLOGY_TITLE.toString());
    }

    @Override
    public String getDisplayName() {
        return getVertex().getProperty(PropertyName.DISPLAY_NAME.toString());
    }

    public Vertex getVertex() {
        return vertex;
    }
}
