package com.altamiracorp.lumify.model;

import com.altamiracorp.lumify.model.graph.GraphRelationship;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;

public class TitanGraphRelationship extends GraphRelationship {

    public TitanGraphRelationship(Edge e) {
        super(e.getId().toString(), e.getVertex(Direction.OUT).getId().toString(), e.getVertex(Direction.IN).getId().toString(), e.getLabel());
    }
}
