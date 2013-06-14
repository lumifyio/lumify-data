package com.altamiracorp.reddawn.ucd.sentence;

import com.altamiracorp.reddawn.model.Column;
import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Repository;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.ucd.term.TermMention;

import java.util.Collection;

public class SentenceRepository extends Repository<Sentence> {
    @Override
    public Sentence fromRow(Row row) {
        Sentence sentence = new Sentence(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(SentenceData.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                sentence.addColumnFamily(new SentenceData().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(SentenceMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                sentence.addColumnFamily(new SentenceMetadata().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().startsWith("urn")) {
                Collection<Column> columns = columnFamily.getColumns();
                sentence.addColumnFamily(new SentenceTerm(columnFamily.getColumnFamilyName()).addColumns(columns));
            } else {
                sentence.addColumnFamily(columnFamily);
            }
        }
        return sentence;
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
