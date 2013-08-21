package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.ucd.sentence.Sentence;

import java.util.ArrayList;
import java.util.List;

public class ExtractedData {
    private List<TermAndGraphVertex> termsAndGraphVertices = new ArrayList<TermAndGraphVertex>();
    private List<Sentence> sentences = new ArrayList<Sentence>();
    private List<StructuredDataRelationship> relationships = new ArrayList<StructuredDataRelationship>();

    public void addTermAndGraphVertex(List<TermAndGraphVertex> termAndGraphVertexes) {
        this.termsAndGraphVertices.addAll(termAndGraphVertexes);
    }

    public List<TermAndGraphVertex> getTermsAndGraphVertices() {
        return termsAndGraphVertices;
    }

    public void addSentence(Sentence sentence) {
        this.sentences.add(sentence);
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public List<StructuredDataRelationship> getRelationships() {
        return relationships;
    }

    public void addRelationships(List<StructuredDataRelationship> relationships) {
        this.relationships.addAll(relationships);
    }
}
