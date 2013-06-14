package com.altamiracorp.reddawn.model;

import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class AccumuloSession extends Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccumuloSession.class.getName());

    public static final String ZOOKEEPER_INSTANCE_NAME = "zookeeperInstanceName";
    public static final String ZOOKEEPER_SERVER_NAMES = "zookeeperServerNames";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    private final Connector connector;
    private long maxMemory = 1000000L;
    private long maxLatency = 1000L;
    private int maxWriteThreads = 10;

    public AccumuloSession(Connector connector, AccumuloQueryUser queryUser) {
        super(queryUser);
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
