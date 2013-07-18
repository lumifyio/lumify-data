package com.altamiracorp.reddawn.ucd.concept;

import com.altamiracorp.reddawn.model.Column;
import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Repository;
import com.altamiracorp.reddawn.model.Row;

import java.util.Collection;

public class ConceptRepository extends Repository<Concept> {
    @Override
    public Concept fromRow(Row row) {
        Concept concept = new Concept(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(ConceptElements.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                concept.addColumnFamily(new ConceptElements().addColumns(columns));
            } else {
                concept.addColumnFamily(columnFamily);
            }
        }
        return concept;
    }

    @Override
    public Row toRow(Concept concept) {
        return concept;
    }

    @Override
    public String getTableName() {
        return Concept.TABLE_NAME;
    }
}
