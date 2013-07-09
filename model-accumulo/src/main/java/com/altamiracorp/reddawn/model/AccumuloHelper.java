package com.altamiracorp.reddawn.model;

import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.RegExFilter;
import org.apache.accumulo.core.util.PeekingIterator;
import org.apache.hadoop.thirdparty.guava.common.collect.Lists;

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

    /**
     * TODO: Write this
     *
     * @param scanner
     * @param colFamOffset
     * @param colFamLimit
     * @param colFamRegex
     * @return
     */
    public static List<ColumnFamily> scannerToColumnFamilies(Scanner scanner,
                                                             long colFamOffset, long colFamLimit, String colFamRegex) {
        List<ColumnFamily> colFams = Lists.newArrayList();
        String rowKey = scanner.getRange().getStartKey().getRow().toString();

        scanner.setBatchSize(100);
        IteratorSetting iter = new IteratorSetting(15, "regExFilter", RegExFilter.class);
        RegExFilter.setRegexs(iter, null, colFamRegex, null, null, false);
        scanner.addScanIterator(iter);

        long count = 0;
        PeekingIterator<Map.Entry<Key, Value>> iterator = new PeekingIterator<Map.Entry<Key, Value>>(scanner.iterator());

        System.out.println(rowKey);
        System.out.println(iterator.peek().getKey().getRow().toString());

        while (iterator.hasNext() && count < colFamOffset + colFamLimit &&
                iterator.peek().getKey().getRow().toString().equals(rowKey)) {
            ColumnFamily colFam = getNextColumnFamily(iterator);

            if (count >= colFamOffset) {
                colFams.add(colFam);
            }

            count++;
        }

        return colFams;
    }

    public static ColumnFamily getNextColumnFamily(PeekingIterator<Map.Entry<Key, Value>> iterator) {
        String colFamName = iterator.peek().getKey().getColumnFamily().toString();
        ColumnFamily colFam = new ColumnFamily(colFamName);

        System.out.println(colFamName);

        while (iterator.peek() != null && iterator.peek().getKey().getColumnFamily().toString().equals(colFamName)) {
            System.out.println(iterator.peek());
            Map.Entry<Key, Value> next = iterator.next();
            colFam.addColumn(new Column(next.getKey().getColumnQualifier().toString(), next.getValue().toString()));
        }

        System.out.println();

        return colFam;
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
