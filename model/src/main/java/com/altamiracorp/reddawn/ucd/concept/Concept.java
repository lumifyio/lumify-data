package com.altamiracorp.reddawn.ucd.concept;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;

public class Concept extends Row<ConceptRowKey> {
    public static final String TABLE_NAME = "Concept";

    public Concept(RowKey rowKey) {
        super(TABLE_NAME, new ConceptRowKey(rowKey.toString()));
    }

    public Concept(String rowKey) {
        super(TABLE_NAME, new ConceptRowKey(rowKey));
    }

    public Concept() {
        super(TABLE_NAME);
    }

    public ConceptElements getConceptElements() {
        ConceptElements conceptElements = get(ConceptElements.NAME);
        if (conceptElements == null) {
            addColumnFamily(new ConceptElements());
        }
        return get(ConceptElements.NAME);
    }
}
