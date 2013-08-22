package com.altamiracorp.reddawn.model.ontology;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public abstract class Relationship {
    public abstract String getId();

    public abstract String getTitle();

    public abstract String getDisplayName();

    public static JSONArray toJson(List<Relationship> relationships) {
        JSONArray json = new JSONArray();
        for (Relationship relationship : relationships) {
            json.put(relationship.toJson());
        }
        return json;
    }

    private JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", getId());
            json.put("title", getTitle());
            json.put("displayName", getDisplayName());
            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
