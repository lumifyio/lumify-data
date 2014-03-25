package com.altamiracorp.lumify.storm.audio;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import com.altamiracorp.lumify.core.ingest.audio.AudioTextExtractionWorker;
import com.altamiracorp.lumify.storm.BaseArtifactProcessingBolt;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;

public class AudioBolt extends BaseArtifactProcessingBolt {
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        try {
            mkdir("/lumify/artifacts/audio");
            mkdir("/lumify/artifacts/audio/mp4");
            mkdir("/lumify/artifacts/audio/ogg");
        } catch (IOException e) {
            collector.reportError(e);
        }
    }

    @Override
    protected String getThreadPrefix() {
        return "audioBoltWorker";
    }

    @Override
    protected ServiceLoader getServiceLoader() {
        return ServiceLoader.load(AudioTextExtractionWorker.class);
    }

    @Override
    protected boolean isLocalFileRequired() {
        return true;
    }

    @Override
    protected File getPrimaryFileFromArchive(File archiveTempDir) {
        throw new RuntimeException("Could not find primary file");
    }

}
