package com.altamiracorp.lumify.model.artifactThumbnails;

import com.altamiracorp.lumify.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;

public class ArtifactThumbnailRepository extends Repository<ArtifactThumbnail> {
    @Override
    public ArtifactThumbnail fromRow(Row row) {
        ArtifactThumbnail artifactThumbnail = new ArtifactThumbnail(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            String columnFamilyName = columnFamily.getColumnFamilyName();
            if (columnFamilyName.equals(ArtifactThumbnailMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifactThumbnail.addColumnFamily(new ArtifactThumbnailMetadata().addColumns(columns));
            } else {
                artifactThumbnail.addColumnFamily(columnFamily);
            }
        }
        return artifactThumbnail;
    }

    @Override
    public Row toRow(ArtifactThumbnail artifactThumbnail) {
        return artifactThumbnail;
    }

    @Override
    public String getTableName() {
        return ArtifactThumbnail.TABLE_NAME;
    }
}