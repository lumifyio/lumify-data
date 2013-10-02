package com.altamiracorp.lumify.textExtraction;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ArtifactExtractedInfo {
    private static final String ROW_KEY = "rowKey";
    private static final String TEXT = "text";
    private static final String TITLE = "title";
    private static final String DATE = "date";
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    public void mergeFrom(ArtifactExtractedInfo artifactExtractedInfo) {
        for (Map.Entry<String, Object> prop : artifactExtractedInfo.properties.entrySet()) {
            this.properties.put(prop.getKey(), prop.getValue());
        }
    }

    public void setRowKey(String rowKey) {
        properties.put(ROW_KEY, rowKey);
    }

    public String getText() {
        return (String) properties.get(TEXT);
    }

    public void setText(String text) {
        properties.put(TEXT, text);
    }

    public void setTitle(String title) {
        properties.put(TITLE, title);
    }

    public void setDate(Date date) {
        properties.put(DATE, date);
    }

    public void set(String key, Object val) {
        properties.put(key, val);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> prop : properties.entrySet()) {
            json.put(prop.getKey(), prop.getValue());
        }
        return json;
    }
}
