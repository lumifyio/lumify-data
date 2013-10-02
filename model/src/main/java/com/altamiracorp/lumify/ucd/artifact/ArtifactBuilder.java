package com.altamiracorp.lumify.ucd.artifact;

import com.altamiracorp.lumify.model.BaseBuilder;
import com.altamiracorp.lumify.model.Column;
import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Row;

import java.util.Collection;

public class ArtifactBuilder extends BaseBuilder<Artifact> {
    public Artifact fromRow(Row row) {
        Artifact artifact = new Artifact(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(ArtifactMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new ArtifactMetadata().addColumns(columns));
            } else {
                artifact.addColumnFamily(columnFamily);
            }
        }
        return artifact;
    }

    @Override
    public String getTableName() {
        return Artifact.TABLE_NAME;
    }
}
