package com.altamiracorp.reddawn.statementExtraction;

import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceTerm;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.statement.StatementArtifact;
import com.altamiracorp.reddawn.ucd.statement.StatementRowKey;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class SentenceBasedStatementExtractor implements StatementExtractor {
    public static final String MODEL_KEY = "urn:mil.army.dsc:schema:dataspace";
    public static final String PREDICATE_LABEL = "co-occured in sentence with";
    public static final String AUTHOR = "SentenceBasedStatementExtractor";

    @Override
    public void setup(Mapper<Text, Sentence, Text, Statement>.Context context) throws IOException {
    }

    @Override
    public Collection<Statement> extractStatements(Sentence sentence) {
        ArrayList<Statement> result = new ArrayList<Statement>();
        int sentenceTermCount = sentence.getSentenceTerms().size();
        if (sentenceTermCount <= 1) return result;

        for (SentenceTerm first : sentence.getSentenceTerms()) {
            for (SentenceTerm second : sentence.getSentenceTerms()) {
                if (first != second) {
                    Statement statement = new Statement(
                            new StatementRowKey(new TermRowKey(first.getTermId().toString()),
                                    new PredicateRowKey(MODEL_KEY, PREDICATE_LABEL),
                                    new TermRowKey(second.getTermId()))
                    );

                    StatementArtifact statementArtifact = new StatementArtifact()
                            .setArtifactKey(sentence.getData().getArtifactId())
                            .setAuthor(AUTHOR)
                            .setDate(getNow().getTime())
                            .setExtractorId(AUTHOR)
                            .setSecurityMarking(sentence.getMetadata().getSecurityMarking())
                            .setSentence(sentence.getRowKey().toString())
                            .setSentenceText(sentence.getData().getText())
                            .setArtifactType(ArtifactType.valueOf(sentence.getMetadata().getArtifactType()));

                    if (sentence.getMetadata().getArtifactSubject() != null) {
                        statementArtifact.setArtifactSubject(sentence.getMetadata().getArtifactSubject());
                    }

                    statement.addStatementArtifact(statementArtifact);

                    result.add(statement);
                }
            }
        }

        return result;
    }

    protected Date getNow() {
        return new Date();
    }
}
