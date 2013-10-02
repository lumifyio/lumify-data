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
    private static final String TEXT_HDFS_PATH = "textHdfsPath";
    private static final String ONTOLOGY_CLASS_URI = "ontologyClassUri";
    private static final String RAW_HDFS_PATH = "rawHdfsPath";
    private static final String RAW = "raw";
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    public void mergeFrom(ArtifactExtractedInfo artifactExtractedInfo) {
        for (Map.Entry<String, Object> prop : artifactExtractedInfo.properties.entrySet()) {
            this.properties.put(prop.getKey(), prop.getValue());
        }
    }

    public void setRowKey(String rowKey) {
        properties.put(ROW_KEY, rowKey);
    }

    public String getRowKey() {
        return (String) properties.get(ROW_KEY);
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

    public void setTextHdfsPath(String textHdfsPath) {
        set(TEXT_HDFS_PATH, textHdfsPath);
    }

    public String getTextHdfsPath() {
        return (String) properties.get(TEXT_HDFS_PATH);
    }

    public void setOntologyClassUri(String ontologyClassUri) {
        set(ONTOLOGY_CLASS_URI, ontologyClassUri);
    }

    public String getOntologyClassUri() {
        return (String) properties.get(ONTOLOGY_CLASS_URI);
    }

    public void setRawHdfsPath(String rawHdfsPath) {
        set(RAW_HDFS_PATH, rawHdfsPath);
    }

    public String getRawHdfsPath() {
        return (String) properties.get(RAW_HDFS_PATH);
    }

    public void setRaw(byte[] raw) {
        set(RAW, raw);
    }

    public byte[] getRaw() {
        return (byte[]) properties.get(RAW);
    }
}
