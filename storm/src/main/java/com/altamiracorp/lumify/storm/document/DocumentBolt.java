package com.altamiracorp.lumify.storm.document;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorker;
import com.altamiracorp.lumify.core.ingest.document.DocumentTextExtractionWorker;
import com.altamiracorp.lumify.core.util.ThreadedInputStreamProcess;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.storm.BaseFileProcessingBolt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class DocumentBolt extends BaseFileProcessingBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentBolt.class.getName());

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        List<ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>> workers = new ArrayList<ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>>();

        ServiceLoader<DocumentTextExtractionWorker> services = ServiceLoader.load(DocumentTextExtractionWorker.class);
        for (TextExtractionWorker service : services) {
            LOGGER.info("adding class " + service.getClass().getName() + " to " + getClass().getName());
            inject(service);
        }
        for (TextExtractionWorker service : services) {
            service.prepare(stormConf, getUser());
        }
        for (TextExtractionWorker service : services) {
            workers.add((ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>) service);
        }

        setThreadedInputStreamProcess(new ThreadedInputStreamProcess<ArtifactExtractedInfo, AdditionalArtifactWorkData>("documentBoltWorkers", workers));
    }
}
