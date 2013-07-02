package com.altamiracorp.reddawn.ucd.artifact;

import com.altamiracorp.reddawn.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;


public class ArtifactRepository extends Repository<Artifact> {
    @Override
    public Row toRow(Artifact artifact) {
        return artifact;
    }

    @Override
    public String getTableName() {
        return Artifact.TABLE_NAME;
    }

    @Override
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
            } else {
                artifact.addColumnFamily(columnFamily);
            }
        }
        return artifact;
    }

    public SaveFileResults saveFile(Session session, InputStream in) {
        return session.saveFile(in);
    }

    public InputStream getRaw(Session session, Artifact artifact) {
        byte[] bytes = artifact.getContent().getDocArtifactBytes();
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        }

        String hdfsPath = artifact.getGenericMetadata().getHdfsFilePath();
        if (hdfsPath != null) {
            return session.loadFile(hdfsPath);
        }

        return null;
    }
}
