package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.term.Term;

import java.util.ArrayList;
import java.util.List;

public class ExtractedData {
    private List<Sentence> sentences = new ArrayList<Sentence>();
    private List<Term> terms = new ArrayList<Term>();
    private List<Statement> statements = new ArrayList<Statement>();

    public void addSentence(Sentence sentence) {
        this.sentences.add(sentence);
    }

    public void addTerms(List<Term> terms) {
        this.terms.addAll(terms);
    }

    public void addStatements(List<Statement> statements) {
        this.statements.addAll(statements);
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public List<Statement> getStatements() {
        return statements;
    }
}
