package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.altamiracorp.reddawn.model.termMention.TermMention;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TermMentionOffsetItem extends OffsetItem implements Comparable<TermMentionOffsetItem> {

    private TermMention termMention;

    public TermMentionOffsetItem(TermMention termMention) {
        this.termMention = termMention;
    }

    @Override
    public long getStart() {
        return termMention.getRowKey().getStartOffset();
    }

    @Override
    public long getEnd() {
        return termMention.getRowKey().getEndOffset();
    }

    @Override
    public String getType() {
        return VertexType.ENTITY.toString();
    }

    public String getConceptGraphVertexId() {
        return termMention.getMetadata().getConceptGraphVertexId();
    }

    @Override
    public String getRowKey() {
        return termMention.getRowKey().toString();
    }

    @Override
    public String getGraphVertexId() {
        return termMention.getMetadata().getGraphVertexId();
    }

    public String getTitle() {
        return termMention.getMetadata().getSign();
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
            infoJson.put("start", getStart());
            infoJson.put("end", getEnd());
            if (getConceptGraphVertexId() != null) {
                infoJson.put("_subType", getConceptGraphVertexId());
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
        if (getGraphVertexId() != null) {
            classes.add("resolved");
        }
        if (getConceptGraphVertexId() != null) {
            classes.add("subType-" + getConceptGraphVertexId());
        }
        return classes;
    }

    @Override
    public int compareTo(TermMentionOffsetItem other) {
        if (this.getStart() == other.getStart()) {
            if (this.getEnd() == other.getEnd()) {
                if (getGraphVertexId() != null && other.getGraphVertexId() == null) {
                    return -1;
                } else if (getGraphVertexId() == null && other.getGraphVertexId() != null) {
                    return 1;
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
