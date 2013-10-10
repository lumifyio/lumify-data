package com.altamiracorp.lumify.storm.structuredDataExtraction;

import com.altamiracorp.lumify.core.ingest.structuredData.StructuredDataExtractionWorker;
import com.altamiracorp.lumify.storm.BaseFileProcessingBolt;

import java.util.ServiceLoader;

public class StructuredDataBolt extends BaseFileProcessingBolt {

    @Override
    protected String getThreadPrefix() {
        return "structuredDataBoltWorker";
    }

    @Override
    protected ServiceLoader getServiceLoader() {
        return ServiceLoader.load(StructuredDataExtractionWorker.class);
    }
}
