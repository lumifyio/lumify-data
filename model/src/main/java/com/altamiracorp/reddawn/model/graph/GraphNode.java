package com.altamiracorp.reddawn.model.graph;

import com.altamiracorp.reddawn.model.ontology.PropertyName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

public abstract class GraphNode {
    public abstract String getId();

    public abstract GraphNode setProperty(String key, Object value);

    public abstract Set<String> getPropertyKeys();

    public Object getProperty(PropertyName propertyKey) {
        return getProperty(propertyKey.toString());
    }

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

    public static JSONArray toJson(List<GraphNode> nodes) {
        JSONArray results = new JSONArray();
        for (GraphNode node : nodes) {
            results.put(node.toJson());
        }
        return results;
    }
}
