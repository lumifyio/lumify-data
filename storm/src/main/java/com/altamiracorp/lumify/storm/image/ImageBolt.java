package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.ingest.image.ImageTextExtractionWorker;
import com.altamiracorp.lumify.storm.BaseFileProcessingBolt;

import java.util.ServiceLoader;


public class ImageBolt extends BaseFileProcessingBolt {
    @Override
    protected String getThreadPrefix() {
        return "imageBoltWorker";
    }

    @Override
    protected ServiceLoader getServiceLoader() {
        return ServiceLoader.load(ImageTextExtractionWorker.class);
    }
}
