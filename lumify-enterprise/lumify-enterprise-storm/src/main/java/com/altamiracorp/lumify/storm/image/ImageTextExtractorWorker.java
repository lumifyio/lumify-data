package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.textExtraction.ImageOcrTextExtractor;
import com.google.inject.Inject;

import java.awt.image.BufferedImage;

public class ImageTextExtractorWorker extends BaseImageWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(ImageTextExtractorWorker.class);
    private ImageOcrTextExtractor imageOcrTextExtractor;

    @Override
    protected ArtifactExtractedInfo doWork(BufferedImage image, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Extracting Image Text [ImageTextExtractorWorker]: %s", data.getFileName());
        ArtifactExtractedInfo info = imageOcrTextExtractor.extractFromImage(image, data.getMimeType());
        if (info == null) {
            return null;
        }
        LOGGER.debug("Finished [ImageTextExtractorWorker]: %s", data.getFileName());
        return info;
    }

    @Inject
    public void setImageOcrTextExtractor(ImageOcrTextExtractor imageOcrTextExtractor) {
        this.imageOcrTextExtractor = imageOcrTextExtractor;
    }


}
