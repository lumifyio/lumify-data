package com.altamiracorp.lumify.storm.video;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import com.altamiracorp.lumify.core.ingest.video.VideoTextExtractionWorker;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.storm.BaseArtifactProcessingBolt;

import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;

public class VideoBolt extends BaseArtifactProcessingBolt {
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        try {
            mkdir("/lumify/artifacts/video");
            mkdir("/lumify/artifacts/video/mp4");
            mkdir("/lumify/artifacts/video/audio");
            mkdir("/lumify/artifacts/video/webm");
            mkdir("/lumify/artifacts/video/posterFrame");
        } catch (IOException e) {
            collector.reportError(e);
        }
    }

    @Override
    protected String getThreadPrefix() {
        return "videoBoltWorker";
    }

    @Override
    protected void onAfterGraphVertexCreated(GraphVertex graphVertex) {
        super.onAfterGraphVertexCreated(graphVertex);

        workQueueRepository.pushProcessedVideo((String) graphVertex.getProperty(PropertyName.ROW_KEY));
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