package com.altamiracorp.lumify.structuredDataExtraction;

import java.util.ArrayList;
import java.util.List;

public class ExtractedData {
    private List<TermAndGraphVertex> termsAndGraphVertices = new ArrayList<TermAndGraphVertex>();
    private List<StructuredDataRelationship> relationships = new ArrayList<StructuredDataRelationship>();

    public void addTermAndGraphVertex(List<TermAndGraphVertex> termAndGraphVertexes) {
        this.termsAndGraphVertices.addAll(termAndGraphVertexes);
    }

    public List<TermAndGraphVertex> getTermsAndGraphVertices() {
        return termsAndGraphVertices;
    }

    public List<StructuredDataRelationship> getRelationships() {
        return relationships;
    }

    public void addRelationships(List<StructuredDataRelationship> relationships) {
        this.relationships.addAll(relationships);
    }
}
