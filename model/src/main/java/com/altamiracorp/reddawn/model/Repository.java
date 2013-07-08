package com.altamiracorp.reddawn.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Repository<T> {

    public abstract T fromRow(Row row);

    public abstract Row toRow(T obj);

    public abstract String getTableName();

    public T findByRowKey(Session session, String rowKey) {
        Row row = session.findByRowKey(getTableName(), rowKey, session.getQueryUser());
        if (row == null) {
            return null;
        }
        return fromRow(row);
    }

    public List<ColumnFamily> findColFamsByRowKeyWithOffset(Session session, String rowKey, long colFamOffset,
                                                           long colFamLimit, String colFamRegex) {
        return session.findByRowKeyWithOffset(getTableName(), rowKey, session.getQueryUser(), colFamOffset,
                colFamLimit, colFamRegex);
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
            results.add(fromRow(row));
        }
        return results;
    }

    public void delete(Session session, RowKey rowKey) {
        session.deleteRow(getTableName(), rowKey);
    }
}
