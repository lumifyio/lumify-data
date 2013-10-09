package com.altamiracorp.lumify.storm.document;

import com.altamiracorp.lumify.core.ingest.document.DocumentTextExtractionWorker;
import com.altamiracorp.lumify.storm.BaseFileProcessingBolt;

import java.util.ServiceLoader;

public class DocumentBolt extends BaseFileProcessingBolt {
    @Override
    protected String getThreadPrefix() {
        return "documentBoltWorker";
    }

    @Override
    protected ServiceLoader getServiceLoader() {
        return ServiceLoader.load(DocumentTextExtractionWorker.class);
    }
}
