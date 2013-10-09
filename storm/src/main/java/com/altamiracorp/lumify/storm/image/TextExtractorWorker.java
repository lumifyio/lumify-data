package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.storm.file.AdditionalWorkData;
import com.altamiracorp.lumify.textExtraction.ArtifactExtractedInfo;
import com.altamiracorp.lumify.textExtraction.ImageOcrTextExtractor;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.core.util.HdfsLimitOutputStream;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class TextExtractorWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalWorkData> {

    private ImageOcrTextExtractor imageOcrTextExtractor;

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalWorkData data) throws Exception {
        ArtifactExtractedInfo info = imageOcrTextExtractor.extractFromImage(getImage(work));
        HdfsLimitOutputStream textOut = new HdfsLimitOutputStream(data.getHdfsFileSystem(), Artifact.MAX_SIZE_OF_INLINE_FILE);

        try{
            textOut.write(info.getText().getBytes());
        } finally {
            textOut.close();
        }
        info.setTextRowKey(textOut.getRowKey());
        if (textOut.hasExceededSizeLimit()) {
            info.setRawHdfsPath(textOut.getHdfsPath().toString());
            info.setText(null);
        }

        return info;
    }

    private BufferedImage getImage(InputStream in) throws IOException {
        return ImageIO.read(in);
    }

    @Inject
    public void setImageOcrTextExtractor (ImageOcrTextExtractor imageOcrTextExtractor) {
        this.imageOcrTextExtractor = imageOcrTextExtractor;
    }
}
