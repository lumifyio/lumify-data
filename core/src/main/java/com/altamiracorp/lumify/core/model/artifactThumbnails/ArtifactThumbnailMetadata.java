package com.altamiracorp.lumify.core.model.artifactThumbnails;

import com.altamiracorp.lumify.core.model.ColumnFamily;
import com.altamiracorp.lumify.core.model.Value;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ArtifactThumbnailMetadata extends ColumnFamily {
    public static final String NAME = "metadata";
    private static final String DATA = "data";

    public ArtifactThumbnailMetadata() {
        super(NAME);
    }

    public byte[] getData() {
        return Value.toBytes(get(DATA));
    }

    public ArtifactThumbnailMetadata setData(byte[] data) {
        set(DATA, data);
        return this;
    }

    public BufferedImage getImage() {
        try {
            byte[] data = getData();
            return ImageIO.read(new ByteArrayInputStream(data));
        } catch (IOException e) {
            throw new RuntimeException("Could not load image", e);
        }
    }
}
