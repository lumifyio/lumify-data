package com.altamiracorp.reddawn.model.ontology;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public abstract class Property {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public abstract String getId();

    public abstract String getTitle();

    public abstract String getDisplayName();

    public abstract PropertyType getDataType();

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
            json.put("dataType", getDataType().toString());
            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Object convertString(String valueStr) throws ParseException {
        PropertyType dateType = getDataType();
        Object value = valueStr;
        switch (dateType) {
            case DATE:
                value = dateFormat.parse(valueStr);
                break;
        }
        return value;
    }
}
