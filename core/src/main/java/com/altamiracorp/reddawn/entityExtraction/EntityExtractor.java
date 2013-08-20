package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import opennlp.tools.util.Span;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Collection;

public abstract class EntityExtractor {
    abstract void setup(Mapper.Context context) throws IOException;

    abstract Collection<Term> extract(Sentence sentence) throws Exception;

    abstract String getModelName();

    abstract String getExtractorId ();

    protected Term createTerm(Sentence sentence, Long charOffset, String entityName, String entityType, int entityStart, int entityEnd) {
        Long termMentionStart = charOffset + entityStart;
        Long termMentionEnd = charOffset + entityEnd;

        TermRowKey termKey = new TermRowKey(entityName, getModelName(), mapConceptName(entityType));
        TermMention termMention = new TermMention()
                .setArtifactKey(sentence.getData().getArtifactId())
                .setArtifactKeySign(sentence.getData().getArtifactId())
                .setAuthor(getExtractorId())
                .setMentionStart(termMentionStart)
                .setMentionEnd(termMentionEnd)
                .setSentenceText(sentence.getData().getText())
                .setSentenceTokenOffset(sentence.getRowKey().getStartOffset())
                .setArtifactType(sentence.getMetadata().getArtifactType());

        if (sentence.getMetadata().getArtifactSubject() != null) {
            termMention.setArtifactSubject(sentence.getMetadata().getArtifactSubject());
        }

        setSecurityMarking(termMention, sentence);
        Term term = new Term(termKey)
                .addTermMention(termMention);
        return term;
    }

    protected String mapConceptName(String concept) {
        return concept.substring(0, 1).toUpperCase() + concept.substring(1);
    }

    private void setSecurityMarking(TermMention termMention, Sentence sentence) {
        String securityMarking = sentence.getMetadata().getSecurityMarking();
        if (securityMarking != null) {
            termMention.setSecurityMarking(sentence.getMetadata().getSecurityMarking());
        }
    }
}
