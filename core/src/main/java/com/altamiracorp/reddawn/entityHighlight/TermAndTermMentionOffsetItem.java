package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.ucd.object.UcdObjectRowKey;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TermAndTermMentionOffsetItem extends OffsetItem {

    private TermAndTermMention termAndTermMention;

    public TermAndTermMentionOffsetItem(TermAndTermMention termAndTermMention) {
        this.termAndTermMention = termAndTermMention;
    }

    @Override
    public Long getStart() {
        return termAndTermMention.getTermMention().getMentionStart();
    }

    @Override
    public Long getEnd() {
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

    public UcdObjectRowKey getObjectRowKey() {
        return termAndTermMention.getTermMention().getObjectRowKey();
    }

    @Override
    public boolean shouldHighlight() {
        if (!super.shouldHighlight()) {
            return false;
        }
        if (termAndTermMention.getTerm().getRowKey().getModelKey().equals(TermRowKey.OBJECT_MODEL_KEY)) {
            return false;
        }
        return true;
    }

    @Override
    public JSONObject getInfoJson() {
        try {
            JSONObject infoJson = super.getInfoJson();
            if (getSubType() != null) {
                infoJson.put("subType", getSubType());
            }
            if (getObjectRowKey() != null) {
                infoJson.put("objectRowKey", getObjectRowKey().toJson());
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
}
