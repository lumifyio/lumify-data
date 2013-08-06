package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.ucd.sentence.Sentence;

public class SentenceOffsetItem extends OffsetItem {

    private final Sentence sentence;

    public SentenceOffsetItem(Sentence sentence) {
        this.sentence = sentence;
    }

    @Override
    public long getStart() {
        return sentence.getRowKey().getStartOffset();
    }

    @Override
    public long getEnd() {
        return sentence.getRowKey().getEndOffset();
    }

    @Override
    public String getType() {
        return "sentence";
    }

    @Override
    public String getRowKey() {
        return sentence.getRowKey().toString();
    }
}
