package com.altamiracorp.lumify.model.artifactThumbnails;

import com.altamiracorp.lumify.model.*;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class ArtifactThumbnailRepository extends Repository<ArtifactThumbnail> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactThumbnailRepository.class.getName());

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

    public byte[] getThumbnailData(Session modelSession, ArtifactRowKey artifactRowKey, String thumbnailType, int width, int height) {
        ArtifactThumbnailRowKey rowKey = new ArtifactThumbnailRowKey(artifactRowKey.toString(), thumbnailType, width, height);
        ArtifactThumbnail artifactThumbnail = findByRowKey(modelSession, rowKey.toString());
        if (artifactThumbnail == null) {
            return null;
        }
        return artifactThumbnail.getMetadata().getData();
    }

    public byte[] createThumbnail(Session modelSession, ArtifactRowKey artifactRowKey, String thumbnailType, InputStream in, int[] boundaryDims) throws IOException {
        BufferedImage originalImage = ImageIO.read(in);
        int[] originalImageDims = new int[]{originalImage.getWidth(), originalImage.getHeight()};
        int[] newImageDims = getScaledDimension(originalImageDims, boundaryDims);

        if (newImageDims[0] >= originalImageDims[0] || newImageDims[1] >= originalImageDims[1]) {
            LOGGER.info("Original image dimensions " + originalImageDims[0] + "x" + originalImageDims[1] + " are smaller "
                    + "than requested dimensions " + newImageDims[0] + "x" + newImageDims[1]
                    + " returning original.");
        }

        BufferedImage resizedImage = new BufferedImage(newImageDims[0], newImageDims[1], originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, resizedImage.getWidth(), resizedImage.getHeight(), null);
        g.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", out);

        saveThumbnail(modelSession, artifactRowKey, thumbnailType, boundaryDims, out.toByteArray());

        return out.toByteArray();
    }

    private void saveThumbnail(Session modelSession, ArtifactRowKey artifactRowKey, String thumbnailType, int[] boundaryDims, byte[] bytes) {
        ArtifactThumbnailRowKey artifactThumbnailRowKey = new ArtifactThumbnailRowKey(artifactRowKey.toString(), thumbnailType, boundaryDims[0], boundaryDims[1]);
        ArtifactThumbnail artifactThumbnail = new ArtifactThumbnail(artifactThumbnailRowKey);
        artifactThumbnail.getMetadata().setData(bytes);
        save(modelSession, artifactThumbnail);
    }

    private static int[] getScaledDimension(int[] imgSize, int[] boundary) {
        int originalWidth = imgSize[0];
        int originalHeight = imgSize[1];
        int boundWidth = boundary[0];
        int boundHeight = boundary[1];
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > boundWidth) {
            newWidth = boundWidth;
            newHeight = (newWidth * originalHeight) / originalWidth;
        }

        if (newHeight > boundHeight) {
            newHeight = boundHeight;
            newWidth = (newHeight * originalWidth) / originalHeight;
        }

        return new int[]{newWidth, newHeight};
    }
}