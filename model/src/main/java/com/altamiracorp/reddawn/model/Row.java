package com.altamiracorp.reddawn.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;

public class Row<TRowKey extends RowKey> {
    private TRowKey rowKey;
    private HashMap<String, ColumnFamily> columnFamilies = new HashMap<String, ColumnFamily>();
    private String tableName;

    public Row(String tableName, TRowKey rowKey) {
        this(tableName);
        this.rowKey = rowKey;
    }

    public Row(String tableName) {
        this.tableName = tableName;
    }

    public TRowKey getRowKey() {
        return rowKey;
    }

    public void addColumnFamily(ColumnFamily columnFamily) {
        this.columnFamilies.put(columnFamily.getColumnFamilyName(), columnFamily);
    }

    public <T extends ColumnFamily> T get(String columnFamilyName) {
        return (T) this.columnFamilies.get(columnFamilyName);
    }

    public Collection<ColumnFamily> getColumnFamilies() {
        return this.columnFamilies.values();
    }

    public String getTableName() {
        return this.tableName;
    }

    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("key", getRowKey().toJson());
            json.put("tableName", getTableName());
            for (ColumnFamily columnFamily : getColumnFamilies()) {
                json.put(columnFamily.getColumnFamilyName(), columnFamily.toJson());
            }
            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
