package com.altamiracorp.lumify.core.model.artifactThumbnails;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.model.Column;
import com.altamiracorp.lumify.core.model.ColumnFamily;
import com.altamiracorp.lumify.core.model.ModelSession;
import com.altamiracorp.lumify.core.model.Repository;
import com.altamiracorp.lumify.core.model.Row;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.google.inject.Inject;

public class ArtifactThumbnailRepository extends Repository<ArtifactThumbnail> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactThumbnailRepository.class);

    @Inject
    public ArtifactThumbnailRepository(final ModelSession modelSession) {
        super(modelSession);
    }

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

    public byte[] getThumbnailData(ArtifactRowKey artifactRowKey, String thumbnailType, int width, int height, User user) {
        ArtifactThumbnailRowKey rowKey = new ArtifactThumbnailRowKey(artifactRowKey.toString(), thumbnailType, width, height);
        ArtifactThumbnail artifactThumbnail = findByRowKey(rowKey.toString(), user);
        if (artifactThumbnail == null) {
            return null;
        }
        return artifactThumbnail.getMetadata().getData();
    }

    public byte[] createThumbnail(ArtifactRowKey artifactRowKey, String thumbnailType, InputStream in, int[] boundaryDims, User user) throws IOException {
        BufferedImage originalImage = ImageIO.read(in);
        int[] originalImageDims = new int[]{originalImage.getWidth(), originalImage.getHeight()};
        int[] newImageDims = getScaledDimension(originalImageDims, boundaryDims);

        if (newImageDims[0] >= originalImageDims[0] || newImageDims[1] >= originalImageDims[1]) {
            LOGGER.info("Original image dimensions " + originalImageDims[0] + "x" + originalImageDims[1] + " are smaller "
                    + "than requested dimensions " + newImageDims[0] + "x" + newImageDims[1]
                    + " returning original.");
        }

        BufferedImage resizedImage = new BufferedImage(newImageDims[0], newImageDims[1], BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, resizedImage.getWidth(), resizedImage.getHeight(), Color.BLACK, null);
        g.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", out);

        saveThumbnail(artifactRowKey, thumbnailType, boundaryDims, out.toByteArray(), user);

        return out.toByteArray();
    }

    private void saveThumbnail(ArtifactRowKey artifactRowKey, String thumbnailType, int[] boundaryDims, byte[] bytes, User user) {
        ArtifactThumbnailRowKey artifactThumbnailRowKey = new ArtifactThumbnailRowKey(artifactRowKey.toString(), thumbnailType, boundaryDims[0], boundaryDims[1]);
        ArtifactThumbnail artifactThumbnail = new ArtifactThumbnail(artifactThumbnailRowKey);
        artifactThumbnail.getMetadata().setData(bytes);
        save(artifactThumbnail, user);
    }

    public static int[] getScaledDimension(int[] imgSize, int[] boundary) {
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