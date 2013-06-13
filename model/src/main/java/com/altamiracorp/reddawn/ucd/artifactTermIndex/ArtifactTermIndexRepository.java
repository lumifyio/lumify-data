package com.altamiracorp.reddawn.ucd.artifactTermIndex;

import com.altamiracorp.reddawn.model.Column;
import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Repository;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;

import java.util.Collection;

public class ArtifactTermIndexRepository extends Repository<ArtifactTermIndex> {
    @Override
    public ArtifactTermIndex fromRow(Row row) {
        ArtifactTermIndex artifactTermIndex = new ArtifactTermIndex(new ArtifactTermIndexRowKey(row.getRowKey().toString()));
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            Collection<Column> columns = columnFamily.getColumns();
            TermRowKey termRowKey = new TermRowKey(columnFamily.getColumnFamilyName());
            artifactTermIndex.addColumnFamily(new ArtifactTermIndexTerm(termRowKey).addColumns(columns));
        }
        return artifactTermIndex;
    }

    @Override
    public Row toRow(ArtifactTermIndex artifactTermIndex) {
        return artifactTermIndex;
    }

    @Override
    public String getTableName() {
        return ArtifactTermIndex.TABLE_NAME;
    }
}
