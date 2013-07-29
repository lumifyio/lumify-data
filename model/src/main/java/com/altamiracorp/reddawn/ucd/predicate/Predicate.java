package com.altamiracorp.reddawn.ucd.predicate;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;

public class Predicate extends Row<PredicateRowKey> {
    public static final String TABLE_NAME = "Predicate";

    public Predicate(RowKey rowKey) {
        super(TABLE_NAME, new PredicateRowKey(rowKey.toString()));
    }

    public Predicate(String rowKey) {
        super(TABLE_NAME, new PredicateRowKey(rowKey));
    }

    public Predicate() {
        super(TABLE_NAME);
    }

    public PredicateElements getPredicateElements() {
        PredicateElements predicateElements = get(PredicateElements.NAME);
        if (predicateElements == null) {
            addColumnFamily(new PredicateElements());
        }
        return get(PredicateElements.NAME);
    }
}
