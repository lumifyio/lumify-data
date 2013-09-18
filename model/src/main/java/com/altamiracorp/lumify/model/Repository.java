package com.altamiracorp.lumify.model;

import com.altamiracorp.lumify.core.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class Repository<T extends Row> {
    private ModelSession session;

    public abstract T fromRow(Row row);

    public abstract Row toRow(T obj);

    public abstract String getTableName();

    public T findByRowKey(String rowKey, User user) {
        Row row = session.findByRowKey(getTableName(), rowKey, user);
        if (row == null) {
            return null;
        }
        T r = fromRow(row);
        r.setDirtyBits(false);
        return r;
    }

    public T findByRowKey(String rowKey, Map<String, String> columnsToReturn, User user) {
        Row row = session.findByRowKey(getTableName(), rowKey, columnsToReturn, user);
        if (row == null) {
            return null;
        }
        T r = fromRow(row);
        r.setDirtyBits(false);
        return r;
    }

    public List<T> findByRowStartsWith(String rowKeyPrefix, User user) {
        Collection<Row> rows = session.findByRowStartsWith(getTableName(), rowKeyPrefix, user);
        return fromRows(rows);
    }

    public List<T> findAll(User user) {
        Collection<Row> rows = session.findByRowStartsWith(getTableName(), null, user);
        return fromRows(rows);
    }

    public void save(T obj) {
        Row r = toRow(obj);
        session.save(r);
    }

    public void saveMany(Collection<T> objs) {
        List<Row> rows = new ArrayList<Row>();
        String tableName = null;
        for (T obj : objs) {
            Row row = toRow(obj);
            if (tableName == null) {
                tableName = row.getTableName();
            }
            rows.add(row);
        }
        session.saveMany(tableName, rows);
    }

    public List<T> fromRows(Collection<Row> rows) {
        ArrayList<T> results = new ArrayList<T>();
        for (Row row : rows) {
            T r = fromRow(row);
            r.setDirtyBits(false);
            results.add(r);
        }
        return results;
    }

    public void delete(RowKey rowKey) {
        session.deleteRow(getTableName(), rowKey);
    }
}
