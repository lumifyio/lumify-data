package com.altamiracorp.lumify.storm.structuredData;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import com.altamiracorp.lumify.core.ingest.structuredData.StructuredDataExtractionWorker;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.storm.BaseArtifactProcessingBolt;

import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;

public class StructuredDataBolt extends BaseArtifactProcessingBolt {

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        try {
            mkdir("/lumify/data/tmp");
        } catch (IOException e) {
            collector.reportError(e);
        }
    }

    @Override
    protected String getThreadPrefix() {
        return "structuredDataBoltWorker";
    }

    @Override
    protected ServiceLoader getServiceLoader() {
        return ServiceLoader.load(StructuredDataExtractionWorker.class);
    }

    @Override
    protected void onAfterGraphVertexCreated(GraphVertex graphVertex) {
        workQueueRepository.pushArtifactHighlight(graphVertex.getId());
    }
}
