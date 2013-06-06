package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.ucd.model.MutationHelpers;
import org.apache.accumulo.core.client.RowIterator;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Workspace {
    public static final String TABLE_NAME = "redDawnWorkspace";
    private static final String COLUMN_FAMILY_NAME_CONTENT = "content";
    private static final String COLUMN_DATA = "data";
    private WorkspaceKey key;
    private String data;

    public WorkspaceKey getKey() {
        return key;
    }

    public void setKey(WorkspaceKey key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Mutation getMutation() {
        Mutation mutation = new Mutation(getKey().toString());
        MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME_CONTENT, COLUMN_DATA, getData());
        return mutation;
    }

    public static List<Workspace> buildFromScanner(Scanner scanner) {
        List<Workspace> results = new ArrayList<Workspace>();
        RowIterator rowIterator = new RowIterator(scanner);
        while (rowIterator.hasNext()) {
            Iterator<Map.Entry<Key, Value>> columns = rowIterator.next();
            results.add(buildFromRow(columns));
        }
        return results;
    }

    private static Workspace buildFromRow(Iterator<Map.Entry<Key, Value>> columns) {
        Workspace result = new Workspace();
        while (columns.hasNext()) {
            Map.Entry<Key, Value> column = columns.next();
            if (result.key == null) {
                result.key = new WorkspaceKey(column.getKey().getRow().toString());
            }
            populateFromColumn(result, column);
        }
        return result;
    }

    private static void populateFromColumn(Workspace workspace, Map.Entry<Key, Value> column) {
        String columnFamily = column.getKey().getColumnFamily().toString();
        String columnQualifier = column.getKey().getColumnQualifier().toString();
        if (Workspace.COLUMN_FAMILY_NAME_CONTENT.equals(columnFamily)) {
            if (Workspace.COLUMN_DATA.equals(columnQualifier)) {
                workspace.data = column.getValue().toString();
            }
        }
    }
}
