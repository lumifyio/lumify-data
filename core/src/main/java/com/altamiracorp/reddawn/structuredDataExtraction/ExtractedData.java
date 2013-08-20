package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.term.Term;

import java.util.ArrayList;
import java.util.List;

public class ExtractedData {
    private List<Term> terms = new ArrayList<Term>();
    private List<Sentence> sentences = new ArrayList<Sentence>();
    private List<StructuredDataRelationship> relationships = new ArrayList<StructuredDataRelationship>();

    public void addTerms(List<Term> terms) {
        this.terms.addAll(terms);
    }

    public List<Term> getTerms() {
        return terms;
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
