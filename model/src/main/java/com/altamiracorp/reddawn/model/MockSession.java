package com.altamiracorp.reddawn.model;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

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
    public List<Row> findByRowKeyRange(String tableName, String keyStart, String keyEnd, QueryUser queryUser) {
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
    List<Row> findByRowStartsWithList(String tableName, List<String> rowKeyPrefixes, QueryUser queryUser) {
        List<Row> rows = this.tables.get(tableName);
        ArrayList<Row> results = new ArrayList<Row>();
        for (Row row : rows) {
            String rowKey = row.getRowKey().toString();
            for (String rowKeyPrefix : rowKeyPrefixes) {
                if (rowKey.startsWith(rowKeyPrefix)) {
                    results.add(row);
                }
            }
        }
        return results;
    }

    @Override
    List<Row> findByRowKeyRegex(String tableName, String rowKeyRegex, QueryUser queryUser) {
        List<Row> rows = this.tables.get(tableName);
        if (rows == null) {
            throw new RuntimeException("Unable to find table " + tableName + ". Did you remember to call initializeTable() in Session.initialieTables()?");
        }

        List<Row> result = new ArrayList<Row>();
        for (Row row : rows) {
            if (!Pattern.matches(rowKeyRegex, row.getRowKey().toString())) {
                result.add(row);
            }
        }
        return result;
    }

    @Override
    Row findByRowKey(String tableName, String rowKey, QueryUser queryUser) {
        List<Row> rows = this.tables.get(tableName);
        if (rows == null) {
            throw new RuntimeException("Unable to find table " + tableName + ". Did you remember to call initializeTable() in Session.initialieTables()?");
        }
        for (Row row : rows) {
            if (row.getRowKey().toString().equals(rowKey)) {
                return row;
            }
        }
        return null;
    }

    @Override
    Row findByRowKey(String tableName, String rowKey, Map<String, String> columnsToReturn, QueryUser queryUser) {
        return findByRowKey(tableName, rowKey, queryUser);
    }

    @Override
    List<ColumnFamily> findByRowKeyWithColumnFamilyRegexOffsetAndLimit(String tableName, String rowKey, QueryUser queryUser, long colFamOffset, long colFamLimit, String colFamRegex) {
        List<Row> rows = this.tables.get(tableName);
        if (rows == null) {
            throw new RuntimeException("Unable to find table " + tableName + ". Did you remember to call initializeTable() in Session.initialieTables()?");
        }

        Row matchedRow = null;
        for (Row row : rows) {
            if (row.getRowKey().toString().equals(rowKey)) {
                matchedRow = row;
                break;
            }
        }

        List<ColumnFamily> result = new ArrayList<ColumnFamily>();
        long count = 0L;
        for (ColumnFamily colFam : (Collection<ColumnFamily>) matchedRow.getColumnFamilies()) {
            if (Pattern.matches(colFamRegex, colFam.getColumnFamilyName())) {
                if (count < colFamOffset + colFamLimit) {
                    if (count >= colFamOffset) {
                        result.add(colFam);
                    }
                } else {
                    break;
                }

                count++;
            }
        }

        return result;
    }

    @Override
    public void initializeTable(String tableName) {
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

    @Override
    public List<String> getTableList() {
        return new ArrayList<String>(this.tables.keySet());
    }
}
