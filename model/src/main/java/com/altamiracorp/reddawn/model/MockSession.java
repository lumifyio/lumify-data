package com.altamiracorp.reddawn.model;

import org.apache.hadoop.thirdparty.guava.common.collect.Lists;

import java.io.*;
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
    List<Row> findByRowKeyRegex(String tableName, String rowKeyRegex, QueryUser queryUser) {
        // TODO: write this
        return null;
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
    List<ColumnFamily> findByRowKeyWithOffset(String tableName, String rowKey, QueryUser queryUser, long colFamOffset, long colFamLimit, String colFamRegex) {
        // TODO: Write this
        return Lists.newArrayList();
    }

    @Override
    void initializeTable(String tableName) {
        this.tables.put(tableName, new ArrayList<Row>());
    }

    @Override
    public void deleteTable(String tableName) {
        this.tables.remove(tableName);
    }

    @Override
    public void deleteRow(String tableName, RowKey rowKey) {
        String rowKeyStr = rowKey.toString();
        List<Row> rows = this.tables.get(tableName);
        for (int i = 0; i < rows.size(); i++) {
            if (rowKeyStr.equals(rows.get(i).getRowKey().toString())) {
                rows.remove(i);
                return;
            }
        }
    }

    @Override
    public SaveFileResults saveFile(InputStream in) {
        try {
            File temp = File.createTempFile("reddawn", ".bin");
            OutputStream out = new FileOutputStream(temp);
            try {
                String rowKey = RowKeyHelper.buildSHA256KeyString(in, out);
                return new SaveFileResults(rowKey, temp.getAbsolutePath());
            } finally {
                out.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException("could not save file", ex);
        }
    }

    @Override
    public InputStream loadFile(String path) {
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public long getFileLength(String path) {
        return new File(path).length();
    }
}
