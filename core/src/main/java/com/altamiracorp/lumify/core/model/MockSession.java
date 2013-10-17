package com.altamiracorp.lumify.core.model;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.RowKeyHelper;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class MockSession extends ModelSession {
    public HashMap<String, List<Row>> tables = new HashMap<String, List<Row>>();

    @Override
    void save(Row row, User user) {
        List<Row> table = tables.get(row.getTableName());
        if (table == null) {
            throw new NullPointerException("Could not find table with name: " + row.getTableName());
        }
        table.add(row);
    }

    @Override
    void saveMany(String tableName, Collection<Row> rows, User user) {
        for (Row r : rows) {
            save(r, user);
        }
    }

    @Override
    public List<Row> findByRowKeyRange(String tableName, String keyStart, String keyEnd, User user) {
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
    List<Row> findByRowStartsWith(String tableName, String rowKeyPrefix, User user) {
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
    public List<Row> findByRowKeyRegex(String tableName, String rowKeyRegex, User user) {
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
    Row findByRowKey(String tableName, String rowKey, User user) {
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
    Row findByRowKey(String tableName, String rowKey, Map<String, String> columnsToReturn, User user) {
        return findByRowKey(tableName, rowKey, user);
    }

    @Override
    public void initializeTable(String tableName, User user) {
        this.tables.put(tableName, new ArrayList<Row>());
    }

    @Override
    public void deleteTable(String tableName, User user) {
        this.tables.remove(tableName);
    }

    @Override
    public void deleteRow(String tableName, RowKey rowKey, User user) {
        String rowKeyStr = rowKey.toString();
        List<Row> rows = this.tables.get(tableName);
        for (int i = 0; i < rows.size(); i++) {
            if (rowKeyStr.equals(rows.get(i).getRowKey().toString())) {
                rows.remove(i);
                return;
            }
        }
    }

    public void deleteColumn(Row row, String tableName, String columnFamily, String columnQualifier, User user) {
        List<ColumnFamily> columnFamilies = (List<ColumnFamily>) row.getColumnFamilies();
        for (int i = 0; i < columnFamilies.size(); i++) {
            if (columnFamilies.get(i).getColumnFamilyName().equals(columnFamily)) {
                List<Column> columns = (List<Column>) columnFamilies.get(i).getColumns();
                for (int j = 0; j < columns.size(); j++) {
                    if (columns.get(j).getName().equals(columnQualifier)) {
                        columns.remove(j);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public SaveFileResults saveFile(InputStream in, User user) {
        try {
            File temp = File.createTempFile("lumify", ".bin");
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
    public InputStream loadFile(String path, User user) {
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public long getFileLength(String path, User user) {
        return new File(path).length();
    }

    @Override
    public List<String> getTableList(User user) {
        return new ArrayList<String>(this.tables.keySet());
    }

    @Override
    public void close() {
    }
}
