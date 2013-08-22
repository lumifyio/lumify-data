package com.altamiracorp.reddawn.model.ontology;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public abstract class Property {
    public abstract String getId();

    public abstract String getTitle();

    public abstract String getDisplayName();

    public static JSONArray toJson(List<Property> properties) {
        JSONArray json = new JSONArray();
        for (Property property : properties) {
            json.put(property.toJson());
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
