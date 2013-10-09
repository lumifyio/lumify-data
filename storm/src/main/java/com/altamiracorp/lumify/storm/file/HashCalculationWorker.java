package com.altamiracorp.lumify.storm.file;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.document.DocumentTextExtractionWorker;
import com.altamiracorp.lumify.core.util.RowKeyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class HashCalculationWorker extends DocumentTextExtractionWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(HashCalculationWorker.class.getName());

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData additionalArtifactWorkData) throws Exception {
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        info.setRowKey(RowKeyHelper.buildSHA256KeyString(work));
        LOGGER.info("Calculated hash: " + info.getRowKey());
        return info;
    }

    @Override
    public String getName() {
        return "hashCalculator";
    }
}
