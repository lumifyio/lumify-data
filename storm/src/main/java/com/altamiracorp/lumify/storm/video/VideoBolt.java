package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.video.VideoTextExtractionWorker;
import com.altamiracorp.lumify.storm.BaseFileProcessingBolt;

import java.util.ServiceLoader;

public class VideoBolt extends BaseFileProcessingBolt {
    @Override
    protected String getThreadPrefix() {
        return "videoBoltWorker";
    }

    @Override
    protected ServiceLoader getServiceLoader() {
        return ServiceLoader.load(VideoTextExtractionWorker.class);
    }

    @Override
    protected boolean isLocalFileRequired() {
        return true;
    }
}
