package com.altamiracorp.lumify.storm.termExtraction;

import com.altamiracorp.lumify.model.graph.GraphVertex;

public class TextExtractedAdditionalWorkData {

    private GraphVertex graphVertex;

    public void setGraphVertex(GraphVertex graphVertex) {
        this.graphVertex = graphVertex;
    }

    public GraphVertex getGraphVertex() {
        return graphVertex;
    }
}
