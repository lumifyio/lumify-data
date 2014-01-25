package com.altamiracorp.lumify.storm.structuredData;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import com.altamiracorp.lumify.core.ingest.structuredData.StructuredDataExtractionWorker;
import com.altamiracorp.lumify.storm.BaseArtifactProcessingBolt;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;

public class StructuredDataTextExtractorBolt extends BaseArtifactProcessingBolt {

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
    protected File getPrimaryFileFromArchive(File archiveTempDir) {
        for (File f : archiveTempDir.listFiles()) {
            if (f.getName().endsWith(StructuredDataContentTypeSorter.MAPPING_JSON_FILE_NAME_SUFFIX)) {
                continue;
            }
            if (! f.getName().startsWith(".")) {
                return f;
            }
        }
        throw new RuntimeException("Could not find primary file");
    }
}
