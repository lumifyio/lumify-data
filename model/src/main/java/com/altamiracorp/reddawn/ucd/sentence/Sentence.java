package com.altamiracorp.reddawn.ucd.sentence;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;

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
}
