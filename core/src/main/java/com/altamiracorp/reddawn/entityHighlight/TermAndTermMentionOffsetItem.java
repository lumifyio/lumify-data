package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
        return VertexType.TERM_MENTION.toString();
    }

    public String getSubType() {
        return termAndTermMention.getTermMention().getGraphSubTypeVertexeId();
    }

    @Override
    public String getRowKey() {
        return termAndTermMention.getTerm().getRowKey().toString();
    }

    @Override
    public String getGraphVertexId() {
        return termAndTermMention.getTermMention().getGraphVertexId();
    }

    @Override
    public String getResolvedGraphVertexId() {
        return termAndTermMention.getTermMention().getResolvedGraphVertexId();
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
                infoJson.put("_subType", getSubType());
            }
            return infoJson;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getCssClasses() {
        List<String> classes = new ArrayList<String>();
        classes.add("entity");
        if (getResolvedGraphVertexId() != null) {
            classes.add("resolved");
        }
        if (getSubType() != null) {
            classes.add("subType-" + getSubType());
        }
        return classes;
    }

    @Override
    public int compareTo(TermAndTermMentionOffsetItem other) {
        if (this.getStart() == other.getStart()) {
            if (this.getEnd() == other.getEnd()) {
                if (getResolvedGraphVertexId() != null && other.getResolvedGraphVertexId() == null) {
                    return -1;
                } else if (getResolvedGraphVertexId() == null && other.getResolvedGraphVertexId() != null) {
                    return 1;
                } else if (this.termAndTermMention.getTerm().getRowKey().getModelKey().equals(TermRowKey.MANUAL_MODEL_KEY)) {
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
