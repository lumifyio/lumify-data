package com.altamiracorp.reddawn.model;

import org.apache.accumulo.core.client.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AccumuloSession extends Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccumuloSession.class.getName());

    public static final String ZOOKEEPER_INSTANCE_NAME = "zookeeperInstanceName";
    public static final String ZOOKEEPER_SERVER_NAMES = "zookeeperServerNames";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String HADOOP_URL = "hadoopUrl";

    private final Connector connector;
    private final FileSystem hdfsFileSystem;
    private final String hdfsRootDir;
    private long maxMemory = 1000000L;
    private long maxLatency = 1000L;
    private int maxWriteThreads = 10;

    public AccumuloSession(Connector connector, FileSystem hdfsFileSystem, String hdfsRootDir, AccumuloQueryUser queryUser) {
        super(queryUser);
        this.hdfsFileSystem = hdfsFileSystem;
        this.hdfsRootDir = hdfsRootDir;
        this.connector = connector;
    }

    @Override
    void save(Row row) {
        try {
            BatchWriter writer = connector.createBatchWriter(row.getTableName(), getMaxMemory(), getMaxLatency(), getMaxWriteThreads());
            AccumuloHelper.addRowToWriter(writer, row);
            writer.flush();
            writer.close();
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        } catch (MutationsRejectedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void saveMany(String tableName, Collection<Row> rows) {
        try {
            BatchWriter writer = connector.createBatchWriter(tableName, getMaxMemory(), getMaxLatency(), getMaxWriteThreads());
            for (Row row : rows) {
                AccumuloHelper.addRowToWriter(writer, row);
            }
            writer.flush();
            writer.close();
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        } catch (MutationsRejectedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    List<Row> findByRowKeyRange(String tableName, String rowKeyStart, String rowKeyEnd, QueryUser queryUser) {
        try {
            Scanner scanner = this.connector.createScanner(tableName, ((AccumuloQueryUser) queryUser).getAuthorizations());
            if (rowKeyStart != null) {
                scanner.setRange(new Range(rowKeyStart, rowKeyEnd));
            }
            return AccumuloHelper.scannerToRows(tableName, scanner);
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    List<Row> findByRowStartsWith(String tableName, String rowKeyPrefix, QueryUser queryUser) {
        return findByRowKeyRange(tableName, rowKeyPrefix, rowKeyPrefix + "ZZZZ", queryUser); // TODO is this the best way?
    }

    @Override
    List<Row> findByRowKeyRegex(String tableName, String rowKeyRegex, QueryUser queryUser) {
        try {
            Scanner scanner = this.connector.createScanner(tableName, ((AccumuloQueryUser) queryUser).getAuthorizations());
            scanner.setRange(new Range());

            IteratorSetting iter = new IteratorSetting(15, "regExFilter", RegExFilter.class);
            RegExFilter.setRegexs(iter, rowKeyRegex, null, null, null, false);
            scanner.addScanIterator(iter);

            return AccumuloHelper.scannerToRows(tableName, scanner);
        } catch(TableNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    Row findByRowKey(String tableName, String rowKey, QueryUser queryUser) {
        try {
            Scanner scanner = this.connector.createScanner(tableName, ((AccumuloQueryUser) queryUser).getAuthorizations());
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
    List<ColumnFamily> findByRowKeyWithColumnFamilyRegexOffsetAndLimit(String tableName, String rowKey, QueryUser queryUser, long colFamOffset, long colFamLimit, String colFamRegex) {
        try {
            Scanner scanner = this.connector.createScanner(tableName, ((AccumuloQueryUser) queryUser).getAuthorizations());
            scanner.setRange(new Range(rowKey));
            return AccumuloHelper.scannerToColumnFamiliesFilteredByRegex(scanner, colFamOffset, colFamLimit, colFamRegex);
        } catch(TableNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void initializeTable(String tableName) {
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
    public void deleteTable(String tableName) {
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
    public void deleteRow(String tableName, RowKey rowKey) {
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
    public SaveFileResults saveFile(InputStream in) {
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
    public InputStream loadFile(String path) {
        try {
            LOGGER.info("Loading file: " + path);
            return this.hdfsFileSystem.open(new Path(path));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public long getFileLength(String path) {
        try {
            return this.hdfsFileSystem.getFileStatus(new Path(path)).getLen();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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
        Mutation mutation = new Mutation(row.getRowKey().toString());
        Collection<ColumnFamily> columnFamilies = row.getColumnFamilies();
        for (ColumnFamily columnFamily : columnFamilies) {
            for (Column column : columnFamily.getColumns()) {
                Value value = new Value(column.getValue().toBytes());
                mutation.put(columnFamily.getColumnFamilyName(), column.getName(), value);
            }
        }
        return mutation;
    }
}
