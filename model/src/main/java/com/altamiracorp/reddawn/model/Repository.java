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

    public List<T> findByRowStartsWith(Session session, String rowKeyPrefix) {
        Collection<Row> rows = session.findByRowStartsWith(getTableName(), rowKeyPrefix, session.getQueryUser());
        return fromRows(rows);
    }

    public void save(Session session, T obj) {
        Row r = toRow(obj);
        session.save(r);
    }

    public List<T> fromRows(Collection<Row> rows) {
        ArrayList<T> results = new ArrayList<T>();
        for (Row row : rows) {
            results.add(fromRow(row));
        }
        return results;
    }
}
