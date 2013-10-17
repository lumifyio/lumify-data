package com.altamiracorp.lumify.model;

import com.altamiracorp.lumify.core.model.*;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.RowKeyHelper;
import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.RegExFilter;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class AccumuloSession extends ModelSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccumuloSession.class.getName());

    public static final String ZOOKEEPER_INSTANCE_NAME = "zookeeperInstanceName";
    public static final String ZOOKEEPER_SERVER_NAMES = "zookeeperServerNames";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String HADOOP_URL = "hadoopUrl";

    private final Connector connector;
    private final FileSystem hdfsFileSystem;
    private final String hdfsRootDir;
    private final TaskInputOutputContext context;
    private long maxMemory = 1000000L;
    private long maxLatency = 1000L;
    private int maxWriteThreads = 10;

    public AccumuloSession(Connector connector, FileSystem hdfsFileSystem, String hdfsRootDir, TaskInputOutputContext context) {
        this.hdfsFileSystem = hdfsFileSystem;
        this.hdfsRootDir = hdfsRootDir;
        this.connector = connector;
        this.context = context;
    }

    @Override
    public void save(Row row, User user) {
        try {
            if (context != null) {
                context.write(new Text(row.getTableName()), row);
            } else {
                BatchWriter writer = connector.createBatchWriter(row.getTableName(), getMaxMemory(), getMaxLatency(), getMaxWriteThreads());
                AccumuloHelper.addRowToWriter(writer, row);
                writer.flush();
                writer.close();
            }
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        } catch (MutationsRejectedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveMany(String tableName, Collection<Row> rows, User user) {
        if (rows.size() == 0) {
            return;
        }
        try {
            if (context != null) {
                Text tableNameText = new Text(tableName);
                for (Row row : rows) {
                    context.write(tableNameText, row);
                }
            } else {
                BatchWriter writer = connector.createBatchWriter(tableName, getMaxMemory(), getMaxLatency(), getMaxWriteThreads());
                for (Row row : rows) {
                    AccumuloHelper.addRowToWriter(writer, row);
                }
                writer.flush();
                writer.close();
            }
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        } catch (MutationsRejectedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Row> findByRowKeyRange(String tableName, String rowKeyStart, String rowKeyEnd, User user) {
        try {
            Scanner scanner = this.connector.createScanner(tableName, ((AccumuloModelAuthorizations) user.getModelAuthorizations()).getAuthorizations());
            if (rowKeyStart != null) {
                scanner.setRange(new Range(rowKeyStart, rowKeyEnd));
            }
            return AccumuloHelper.scannerToRows(tableName, scanner);
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Row> findByRowStartsWith(String tableName, String rowKeyPrefix, User user) {
        return findByRowKeyRange(tableName, rowKeyPrefix, rowKeyPrefix + "ZZZZ", user); // TODO is this the best way?
    }

    @Override
    public List<Row> findByRowKeyRegex(String tableName, String rowKeyRegex, User user) {
        try {
            Scanner scanner = this.connector.createScanner(tableName, ((AccumuloModelAuthorizations) user.getModelAuthorizations()).getAuthorizations());
            scanner.setRange(new Range());

            IteratorSetting iter = new IteratorSetting(15, "regExFilter", RegExFilter.class);
            RegExFilter.setRegexs(iter, rowKeyRegex, null, null, null, false);
            scanner.addScanIterator(iter);

            return AccumuloHelper.scannerToRows(tableName, scanner);
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Row findByRowKey(String tableName, String rowKey, User user) {
        try {
            Scanner scanner = this.connector.createScanner(tableName, ((AccumuloModelAuthorizations) user.getModelAuthorizations()).getAuthorizations());
            scanner.setRange(new Range(rowKey));
            List<Row> rows = AccumuloHelper.scannerToRows(tableName, scanner);
            if (rows.size() == 0) {
                return null;
            }
            if (rows.size() > 1) {
                throw new RuntimeException("Too many rows returned for a single row query (rowKey: " + rowKey + ", size: " + rows.size() + ")");
            }
            return rows.get(0);
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Row findByRowKey(String tableName, String rowKey, Map<String, String> columnsToReturn, User user) {
        try {
            Scanner scanner = this.connector.createScanner(tableName, ((AccumuloModelAuthorizations) user.getModelAuthorizations()).getAuthorizations());
            scanner.setRange(new Range(rowKey));
            for (Map.Entry<String, String> columnFamilyAndColumnQualifier : columnsToReturn.entrySet()) {
                if (columnFamilyAndColumnQualifier.getValue().equals("*")) {
                    scanner.fetchColumnFamily(new Text(columnFamilyAndColumnQualifier.getKey()));
                } else {
                    scanner.fetchColumn(new Text(columnFamilyAndColumnQualifier.getKey()), new Text(columnFamilyAndColumnQualifier.getValue()));
                }
            }
            List<Row> rows = AccumuloHelper.scannerToRows(tableName, scanner);
            if (rows.size() == 0) {
                return null;
            }
            if (rows.size() > 1) {
                throw new RuntimeException("Too many rows returned for a single row query (rowKey: " + rowKey + ", size: " + rows.size() + ")");
            }
            return rows.get(0);
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initializeTable(String tableName, User user) {
        LOGGER.info("initializeTable: " + tableName);
        try {
            if (!connector.tableOperations().exists(tableName)) {
                connector.tableOperations().create(tableName);
            }
        } catch (AccumuloSecurityException e) {
            throw new RuntimeException(e);
        } catch (TableExistsException e) {
            throw new RuntimeException(e);
        } catch (AccumuloException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteTable(String tableName, User user) {
        LOGGER.info("deleteTable: " + tableName);
        try {
            if (connector.tableOperations().exists(tableName)) {
                connector.tableOperations().delete(tableName);
            }
        } catch (AccumuloSecurityException e) {
            throw new RuntimeException(e);
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        } catch (AccumuloException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteRow(String tableName, RowKey rowKey, User user) {
        LOGGER.info("deleteRow: " + rowKey);
        try {
            // TODO: Find a better way to delete a single row given the row key
            String strRowKey = rowKey.toString();
            char lastChar = strRowKey.charAt(strRowKey.length() - 1);
            char asciiCharBeforeLastChar = (char) (((int) lastChar) - 1);
            String precedingRowKey = strRowKey.substring(0, strRowKey.length() - 1) + asciiCharBeforeLastChar;
            Text startRowKey = new Text(precedingRowKey);
            Text endRowKey = new Text(strRowKey);
            connector.tableOperations().deleteRows(tableName, startRowKey, endRowKey);
        } catch (AccumuloException e) {
            throw new RuntimeException(e);
        } catch (AccumuloSecurityException e) {
            throw new RuntimeException(e);
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SaveFileResults saveFile(InputStream in, User user) {
        try {
            String dataRoot = hdfsRootDir + "/data/";
            FsPermission fsPermission = new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL);
            if (!this.hdfsFileSystem.exists(new Path(dataRoot))) {
                this.hdfsFileSystem.mkdirs(new Path(dataRoot), fsPermission);
            }

            String tempRoot = hdfsRootDir + "/temp/";
            if (!this.hdfsFileSystem.exists(new Path(tempRoot))) {
                this.hdfsFileSystem.mkdirs(new Path(tempRoot), fsPermission);
            }

            String tempPath = tempRoot + UUID.randomUUID().toString();
            FSDataOutputStream out = this.hdfsFileSystem.create(new Path(tempPath));
            String rowKey;
            try {
                rowKey = RowKeyHelper.buildSHA256KeyString(in, out);
            } finally {
                out.close();
            }

            String rowKeyNoSpecialChars = rowKey.replaceAll("" + RowKeyHelper.MINOR_FIELD_SEPARATOR, "").replaceAll("\\x1F", "");
            String path = dataRoot + rowKeyNoSpecialChars;
            this.hdfsFileSystem.rename(new Path(tempPath), new Path(path));
            LOGGER.info("file saved: " + path);
            return new SaveFileResults(rowKey, "/data/" + rowKeyNoSpecialChars);
        } catch (IOException ex) {
            throw new RuntimeException("could not save file to HDFS", ex);
        }
    }

    @Override
    public void deleteColumn(Row row, String tableName, String columnFamily, String columnQualifier, User user) {
        LOGGER.info("delete column: " + columnQualifier + " from columnFamily: " + columnFamily + ", row: " + row.getRowKey().toString());
        try {
            BatchWriter writer = connector.createBatchWriter(tableName, getMaxMemory(), getMaxLatency(), getMaxWriteThreads());
            Mutation mutation = createMutationFromRow(row);
            mutation.putDelete(new Text(columnFamily), new Text(columnQualifier));
            writer.addMutation(mutation);
            writer.flush();
            writer.close();
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        } catch (MutationsRejectedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream loadFile(String path, User user) {
        try {
            LOGGER.info("Loading file: " + path);
            return this.hdfsFileSystem.open(new Path(path));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public long getFileLength(String path, User user) {
        try {
            return this.hdfsFileSystem.getFileStatus(new Path(path)).getLen();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<String> getTableList(User user) {
        return new ArrayList<String>(this.connector.tableOperations().list());
    }

    @Override
    public void close() {
        // TODO: close me
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public long getMaxLatency() {
        return maxLatency;
    }

    public void setMaxLatency(long maxLatency) {
        this.maxLatency = maxLatency;
    }

    public int getMaxWriteThreads() {
        return maxWriteThreads;
    }

    public void setMaxWriteThreads(int maxWriteThreads) {
        this.maxWriteThreads = maxWriteThreads;
    }

    public static Mutation createMutationFromRow(Row row) {
        Mutation mutation = null;
        Collection<ColumnFamily> columnFamilies = row.getColumnFamilies();
        for (ColumnFamily columnFamily : columnFamilies) {
            for (Column column : columnFamily.getColumns()) {
                if (column.isDirty()) {
                    Value value = new Value(column.getValue().toBytes());
                    if (mutation == null) {
                        mutation = new Mutation(row.getRowKey().toString());
                    }
                    mutation.put(columnFamily.getColumnFamilyName(), column.getName(), value);
                }
            }
        }
        return mutation;
    }
}
