package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.ingest.image.ImageTextExtractionWorker;
import com.altamiracorp.lumify.storm.BaseArtifactProcessingBolt;

import java.util.ServiceLoader;


public class ImageBolt extends BaseArtifactProcessingBolt {
    @Override
    protected String getThreadPrefix() {
        return "imageBoltWorker";
    }

    @Override
    protected ServiceLoader getServiceLoader() {
        return ServiceLoader.load(ImageTextExtractionWorker.class);
    }
}
