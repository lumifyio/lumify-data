package com.altamiracorp.lumify.model.termMention;

import com.altamiracorp.lumify.model.*;

import java.util.Collection;
import java.util.List;

public class TermMentionRepository extends Repository<TermMention> {
    @Override
    public TermMention fromRow(Row row) {
        TermMention termMention = new TermMention(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(TermMentionMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                termMention.addColumnFamily(new TermMentionMetadata().addColumns(columns));
            } else {
                termMention.addColumnFamily(columnFamily);
            }
        }
        return termMention;
    }

    @Override
    public Row toRow(TermMention obj) {
        return obj;
    }

    @Override
    public String getTableName() {
        return TermMention.TABLE_NAME;
    }

    public List<TermMention> findByArtifactRowKey(ModelSession session, String artifactRowKey) {
        return findByRowStartsWith(session, artifactRowKey + ":");
    }
}
