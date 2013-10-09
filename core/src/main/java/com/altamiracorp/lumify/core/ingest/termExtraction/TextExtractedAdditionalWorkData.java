package com.altamiracorp.lumify.core.ingest.termExtraction;

import com.altamiracorp.lumify.core.model.graph.GraphVertex;

public class TextExtractedAdditionalWorkData {

    private GraphVertex graphVertex;

    public void setGraphVertex(GraphVertex graphVertex) {
        this.graphVertex = graphVertex;
    }

    public GraphVertex getGraphVertex() {
        return graphVertex;
    }
}
