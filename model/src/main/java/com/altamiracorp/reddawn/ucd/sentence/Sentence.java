package com.altamiracorp.reddawn.ucd.sentence;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.ucd.term.TermMention;

import java.util.ArrayList;
import java.util.List;

public class Sentence extends Row<SentenceRowKey> {
    public static final String TABLE_NAME = "Sentence";

    public Sentence(RowKey rowKey) {
        super(TABLE_NAME, new SentenceRowKey(rowKey.toString()));
    }

    public Sentence(String artifactRowKey, int startOffset, int endOffset) {
        super(TABLE_NAME, new SentenceRowKey(artifactRowKey, startOffset, endOffset));
    }

    public Sentence() {
        super(TABLE_NAME);
    }

    @Override
    public SentenceRowKey getRowKey() {
        SentenceData data = getData();
        return new SentenceRowKey(data.getArtifactId(), data.getStart(), data.getEnd());
    }

    public SentenceData getData() {
        SentenceData sentenceData = get(SentenceData.NAME);
        if (sentenceData == null) {
            addColumnFamily(new SentenceData());
        }
        return get(SentenceData.NAME);
    }

    public SentenceMetadata getMetadata() {
        SentenceMetadata sentenceMetadata = get(SentenceMetadata.NAME);
        if (sentenceMetadata == null) {
            addColumnFamily(new SentenceMetadata());
        }
        return get(SentenceMetadata.NAME);
    }

    public Sentence addSentenceTerm(SentenceTerm sentenceTerm) {
        this.addColumnFamily(sentenceTerm);
        return this;
    }

    public List<SentenceTerm> getSentenceTerms() {
        ArrayList<SentenceTerm> termMentions = new ArrayList<SentenceTerm>();
        for (ColumnFamily columnFamily : getColumnFamilies()) {
            if (columnFamily instanceof SentenceTerm) {
                termMentions.add((SentenceTerm) columnFamily);
            }
        }
        return termMentions;
    }
}
