package com.altamiracorp.lumify.model.termMention;

import java.util.Collection;
import java.util.List;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.Column;
import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.Row;
import com.google.inject.Inject;

public class TermMentionRepository extends Repository<TermMention> {
    @Inject
    public TermMentionRepository(final ModelSession modelSession) {
        super(modelSession);
    }

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

    public List<TermMention> findByArtifactRowKey(String artifactRowKey, User user) {
        return findByRowStartsWith(artifactRowKey + ":", user);
    }
}
