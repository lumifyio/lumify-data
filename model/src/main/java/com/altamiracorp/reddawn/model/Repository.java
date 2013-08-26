package com.altamiracorp.reddawn.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class Repository<T extends Row> {
    public abstract T fromRow(Row row);

    public abstract Row toRow(T obj);

    public abstract String getTableName();

    public T findByRowKey(Session session, String rowKey) {
        Row row = session.findByRowKey(getTableName(), rowKey, session.getQueryUser());
        if (row == null) {
            return null;
        }
        T r = fromRow(row);
        r.setDirtyBits(false);
        return r;
    }

    public T findByRowKey(Session session, String rowKey, Map<String, String> columnsToReturn) {
        Row row = session.findByRowKey(getTableName(), rowKey, columnsToReturn, session.getQueryUser());
        if (row == null) {
            return null;
        }
        T r = fromRow(row);
        r.setDirtyBits(false);
        return r;
    }

    public List<T> findByRowStartsWith(Session session, String rowKeyPrefix) {
        Collection<Row> rows = session.findByRowStartsWith(getTableName(), rowKeyPrefix, session.getQueryUser());
        return fromRows(rows);
    }

    public List<T> findAll(Session session) {
        Collection<Row> rows = session.findByRowStartsWith(getTableName(), null, session.getQueryUser());
        return fromRows(rows);
    }

    public void save(Session session, T obj) {
        Row r = toRow(obj);
        session.save(r);
    }

    public void saveMany(Session session, Collection<T> objs) {
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

    public void delete(Session session, RowKey rowKey) {
        session.deleteRow(getTableName(), rowKey);
    }
}
