package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.model.RowKeyHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class OffsetItem {
    public abstract long getStart();

    public abstract long getEnd();

    public abstract String getType();

    public abstract String getRowKey();

    public String getGraphNodeId() {
        return null;
    }

    public String getResolvedGraphNodeId() {
        return null;
    }

    public JSONObject getInfoJson() {
        try {
            JSONObject infoJson = new JSONObject();
            infoJson.put("start", getStart());
            infoJson.put("end", getEnd());
            infoJson.put("_rowKey", RowKeyHelper.jsonEncode(getRowKey()));
            if (getGraphNodeId() != null) {
                infoJson.put("graphNodeId", getGraphNodeId());
            }
            if (getResolvedGraphNodeId() != null) {
                infoJson.put("resolvedGraphNodeId", getResolvedGraphNodeId());
            }
            infoJson.put("type", getType());
            return infoJson;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getCssClasses() {
        ArrayList<String> classes = new ArrayList<String>();
        classes.add(getType());
        if (getResolvedGraphNodeId() != null) {
            classes.add("resolved");
        }
        return classes;
    }

    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("info", getInfoJson());

            JSONArray cssClasses = new JSONArray();
            for (String cssClass : getCssClasses()) {
                cssClasses.put(cssClass);
            }
            json.put("cssClasses", cssClasses);
            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean shouldHighlight() {
        return true;
    }

    public String getTitle() {
        return null;
    }
}
