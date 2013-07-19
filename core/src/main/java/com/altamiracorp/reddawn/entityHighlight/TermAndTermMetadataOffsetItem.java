package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.ucd.object.UcdObjectRowKey;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;

public class TermAndTermMetadataOffsetItem extends OffsetItem {

    private TermAndTermMention termAndTermMetadata;

    public TermAndTermMetadataOffsetItem(TermAndTermMention termAndTermMetadata) {
        this.termAndTermMetadata = termAndTermMetadata;
    }

    @Override
    public Long getStart() {
        return termAndTermMetadata.getTermMention().getMentionStart();
    }

    @Override
    public Long getEnd() {
        return termAndTermMetadata.getTermMention().getMentionEnd();
    }

    @Override
    public String getType() {
        return "entity";
    }

    @Override
    public String getSubType() {
        return termAndTermMetadata.getTerm().getRowKey().getConceptLabel();
    }

    @Override
    public String getRowKey() {
        return termAndTermMetadata.getTerm().getRowKey().toString();
    }

    @Override
    public String getConceptLabel() {
        return termAndTermMetadata.getTerm().getRowKey().getConceptLabel();
    }

    @Override
    public UcdObjectRowKey getObjectRowKey() {
        return termAndTermMetadata.getTermMention().getObjectRowKey();
    }
}
