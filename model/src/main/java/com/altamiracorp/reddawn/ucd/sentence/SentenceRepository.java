package com.altamiracorp.reddawn.ucd.sentence;

import com.altamiracorp.reddawn.model.Column;
import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Repository;
import com.altamiracorp.reddawn.model.Row;

import java.util.Collection;

public class SentenceRepository extends Repository<Sentence> {
    @Override
    public Sentence fromRow(Row row) {
        Sentence source = new Sentence(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(SentenceData.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                source.addColumnFamily(new SentenceData().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(SentenceMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                source.addColumnFamily(new SentenceMetadata().addColumns(columns));
            } else {
                source.addColumnFamily(columnFamily);
            }
        }
        return source;
    }

    @Override
    public Row toRow(Sentence sentence) {
        return sentence;
    }

    @Override
    public String getTableName() {
        return Sentence.TABLE_NAME;
    }
}
