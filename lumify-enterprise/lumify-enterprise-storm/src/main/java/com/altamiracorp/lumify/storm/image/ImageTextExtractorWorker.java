package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.util.HdfsLimitOutputStream;
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
        HdfsLimitOutputStream textOut = new HdfsLimitOutputStream(data.getHdfsFileSystem(), Artifact.MAX_SIZE_OF_INLINE_FILE);

        try {
            if (info.getText() != null) {
                textOut.write(info.getText().getBytes());
            }
        } finally {
            textOut.close();
        }
        info.setTextRowKey(textOut.getRowKey());
        if (textOut.hasExceededSizeLimit()) {
            info.setRawHdfsPath(textOut.getHdfsPath().toString());
            info.setText(null);
        }
        LOGGER.debug("Finished [ImageTextExtractorWorker]: %s", data.getFileName());
        return info;
    }

    @Inject
    public void setImageOcrTextExtractor(ImageOcrTextExtractor imageOcrTextExtractor) {
        this.imageOcrTextExtractor = imageOcrTextExtractor;
    }


}
