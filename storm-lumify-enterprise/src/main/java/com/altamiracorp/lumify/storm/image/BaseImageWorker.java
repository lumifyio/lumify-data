package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.image.ImageTextExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public abstract class BaseImageWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements ImageTextExtractionWorker {

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        return doWork(getImage(work),data);
    }


    protected BufferedImage getImage(InputStream in) throws IOException {
        return ImageIO.read(in);
    }

    protected abstract ArtifactExtractedInfo doWork (BufferedImage image, AdditionalArtifactWorkData data) throws Exception;

    @Override
    public void prepare(Map stormConf, User user) {
        //no-op
    }
}
