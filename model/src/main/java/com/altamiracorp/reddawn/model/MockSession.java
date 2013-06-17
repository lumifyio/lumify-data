package com.altamiracorp.reddawn.model;

import java.util.*;

public class MockSession extends Session {
    public HashMap<String, List<Row>> tables = new HashMap<String, List<Row>>();

    public MockSession() {
        super(new QueryUser());
    }

    @Override
    void save(Row row) {
        List<Row> table = tables.get(row.getTableName());
        if (table == null) {
            throw new NullPointerException("Could not find table with name: " + row.getTableName());
        }
        table.add(row);
    }

    @Override
    void saveMany(String tableName, Collection<Row> rows) {
        for (Row r : rows) {
            save(r);
        }
    }

    @Override
    List<Row> findByRowKeyRange(String tableName, String keyStart, String keyEnd, QueryUser queryUser) {
        List<Row> rows = this.tables.get(tableName);
        ArrayList<Row> results = new ArrayList<Row>();
        for (Row row : rows) {
            String rowKey = row.getRowKey().toString();
            if (rowKey.compareTo(keyStart) >= 0 && rowKey.compareTo(keyEnd) < 0) {
                results.add(row);
            }
        }
        Collections.sort(results, new RowKeyComparator());
        return results;
    }

    @Override
    List<Row> findByRowStartsWith(String tableName, String rowKeyPrefix, QueryUser queryUser) {
        List<Row> rows = this.tables.get(tableName);
        ArrayList<Row> results = new ArrayList<Row>();
        for (Row row : rows) {
            String rowKey = row.getRowKey().toString();
            if (rowKey.startsWith(rowKeyPrefix)) {
                results.add(row);
            }
        }
        Collections.sort(results, new RowKeyComparator());
        return results;
    }

    @Override
    Row findByRowKey(String tableName, String rowKey, QueryUser queryUser) {
        List<Row> rows = this.tables.get(tableName);
        if (rows == null)
            throw new RuntimeException("Unable to find table " + tableName + ". Did you remember to call initializeTable() in Session.initialieTables()?");
        for (Row row : rows) {
            if (row.getRowKey().toString().equals(rowKey)) {
                return row;
            }
        }
        return null;
    }

    @Override
    void initializeTable(String tableName) {
        this.tables.put(tableName, new ArrayList<Row>());
    }

    @Override
    public void deleteTable(String tableName) {
        this.tables.remove(tableName);
    }
}
