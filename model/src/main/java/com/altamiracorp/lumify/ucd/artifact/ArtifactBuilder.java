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
            if (columnFamily.getColumnFamilyName().equals(ArtifactContent.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new ArtifactContent().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(ArtifactGenericMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new ArtifactGenericMetadata().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(ArtifactDynamicMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new ArtifactDynamicMetadata().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(ArtifactDetectedObjects.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new ArtifactDetectedObjects().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(ArtifactExtractedText.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new ArtifactExtractedText().addColumns(columns));
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
