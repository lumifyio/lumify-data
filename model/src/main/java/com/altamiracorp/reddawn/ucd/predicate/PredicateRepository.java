package com.altamiracorp.reddawn.ucd.predicate;

import com.altamiracorp.reddawn.model.*;

import java.util.Collection;

public class PredicateRepository extends Repository<Predicate> {
    @Override
    public Predicate fromRow(Row row) {
        Predicate predicate = new Predicate(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(PredicateElements.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                predicate.addColumnFamily(new PredicateElements().addColumns(columns));
            } else {
                predicate.addColumnFamily(columnFamily);
            }
        }
        return predicate;
    }

    @Override
    public Row toRow(Predicate predicate) {
        return predicate;
    }

    @Override
    public String getTableName() {
        return Predicate.TABLE_NAME;
    }

    public void save(Session session, PredicateRowKey predicateRowKey, String labelUi) {
        Predicate predicate = new Predicate(predicateRowKey);
        predicate.getPredicateElements()
                .setLabelUi(labelUi);
        save(session, predicate);
    }
}
