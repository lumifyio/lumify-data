package com.altamiracorp.reddawn.model;

import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.RowIterator;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.util.PeekingIterator;

import java.util.*;

public class AccumuloHelper {
    public static void addRowToWriter(BatchWriter writer, Row row) throws MutationsRejectedException {
        Mutation mutation = new Mutation(row.getRowKey().toString());
        Collection<ColumnFamily> columnFamilies = row.getColumnFamilies();
        for (ColumnFamily columnFamily : columnFamilies) {
            addColumnFamilyToMutation(mutation, columnFamily);
        }
        writer.addMutation(mutation);
    }

    private static void addColumnFamilyToMutation(Mutation mutation, ColumnFamily columnFamily) {
        for (Column column : columnFamily.getColumns()) {
            addColumnToMutation(mutation, column, columnFamily.getColumnFamilyName());
        }
    }

    private static void addColumnToMutation(Mutation mutation, Column column, String columnFamilyName) {
        com.altamiracorp.reddawn.model.Value v = column.getValue();
        Value value = null;
        if (v != null) {
            value = new Value(v.toBytes());
        }
        mutation.put(columnFamilyName, column.getName(), value);
    }

    public static List<Row> scannerToRows(String tableName, Scanner scanner) {
        ArrayList<Row> rows = new ArrayList<Row>();
        RowIterator rowIterator = new RowIterator(scanner);
        while (rowIterator.hasNext()) {
            Iterator<Map.Entry<Key, Value>> row = rowIterator.next();
            rows.add(accumuloRowToRow(tableName, row));
        }
        return rows;
    }

    public static Row accumuloRowToRow(String tableName, Iterator<Map.Entry<Key, Value>> accumuloRow) {
        Row<RowKey> row = null;
        while (accumuloRow.hasNext()) {
            Map.Entry<Key, Value> accumuloColumn = accumuloRow.next();
            if (row == null) {
                String rowKey = accumuloColumn.getKey().getRow().toString();
                row = new Row<RowKey>(tableName, new RowKey(rowKey));
            }
            String columnFamilyString = accumuloColumn.getKey().getColumnFamily().toString();
            ColumnFamily columnFamily = row.get(columnFamilyString);
            if (columnFamily == null) {
                row.addColumnFamily(new ColumnFamily(columnFamilyString));
                columnFamily = row.get(columnFamilyString);
            }

            String columnNameString = accumuloColumn.getKey().getColumnQualifier().toString();
            columnFamily.set(columnNameString, accumuloValueToObject(accumuloColumn.getValue()));
        }
        return row;
    }

    private static byte[] accumuloValueToObject(Value value) {
        return value.get();
    }
}
