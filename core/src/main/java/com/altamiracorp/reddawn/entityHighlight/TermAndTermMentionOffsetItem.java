package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TermAndTermMentionOffsetItem extends OffsetItem implements Comparable<TermAndTermMentionOffsetItem> {

    private TermAndTermMention termAndTermMention;

    public TermAndTermMentionOffsetItem(TermAndTermMention termAndTermMention) {
        this.termAndTermMention = termAndTermMention;
    }

    @Override
    public long getStart() {
        return termAndTermMention.getTermMention().getMentionStart();
    }

    @Override
    public long getEnd() {
        return termAndTermMention.getTermMention().getMentionEnd();
    }

    @Override
    public String getType() {
        return "entity";
    }

    public String getSubType() {
        return termAndTermMention.getTerm().getRowKey().getConceptLabel();
    }

    @Override
    public String getRowKey() {
        return termAndTermMention.getTerm().getRowKey().toString();
    }

    @Override
    public String getGraphNodeId() {
        return termAndTermMention.getTermMention().getGraphNodeId(termAndTermMention.getTerm());
    }

    public String getConceptLabel() {
        return termAndTermMention.getTerm().getRowKey().getConceptLabel();
    }

    public String getTitle() {
        return termAndTermMention.getTerm().getRowKey().getSign();
    }

    @Override
    public boolean shouldHighlight() {
        if (!super.shouldHighlight()) {
            return false;
        }
        return true;
    }

    @Override
    public JSONObject getInfoJson() {
        try {
            JSONObject infoJson = super.getInfoJson();
            infoJson.put("title", getTitle());
            if (getSubType() != null) {
                infoJson.put("subType", getSubType());
            }
            return infoJson;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getCssClasses() {
        List<String> classes = super.getCssClasses();
        classes.add(getConceptLabel());
        return classes;
    }

    @Override
    public int compareTo(TermAndTermMentionOffsetItem other) {
        if (this.getStart() == other.getStart()) {
            if (this.getEnd() == other.getEnd()) {
                // TODO: probably should tag nodes that are resolved somehow so that we can sort them better
                if (this.termAndTermMention.getTerm().getRowKey().getModelKey().equals(PredicateRowKey.MANUAL_MODEL_KEY)) {
                    return -1;
                }
                return 0;
            } else {
                return this.getEnd() < other.getEnd() ? -1 : 1;
            }
        } else {
            return this.getStart() < other.getStart() ? -1 : 1;
        }
    }
}
