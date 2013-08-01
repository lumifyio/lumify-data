package com.altamiracorp.reddawn.model.graph;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public abstract class GraphNode {
    public abstract String getId();

    public abstract void setProperty(String key, Object value);

    public abstract Set<String> getPropertyKeys();

    public abstract Object getProperty(String propertyKey);

    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", getId());
            JSONObject propertiesJson = new JSONObject();
            for (String key : getPropertyKeys()) {
                propertiesJson.put(key, getProperty(key));
            }
            json.put("properties", propertiesJson);
            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
