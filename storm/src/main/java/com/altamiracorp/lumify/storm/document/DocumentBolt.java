package com.altamiracorp.lumify.storm.document;

import com.altamiracorp.lumify.core.ingest.document.DocumentTextExtractionWorker;
import com.altamiracorp.lumify.storm.BaseArtifactProcessingBolt;

import java.util.ServiceLoader;

public class DocumentBolt extends BaseArtifactProcessingBolt {
    @Override
    protected String getThreadPrefix() {
        return "documentBoltWorker";
    }

    @Override
    protected ServiceLoader getServiceLoader() {
        return ServiceLoader.load(DocumentTextExtractionWorker.class);
    }
}
