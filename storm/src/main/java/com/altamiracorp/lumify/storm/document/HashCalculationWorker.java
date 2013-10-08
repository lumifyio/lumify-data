package com.altamiracorp.lumify.storm.document;

import com.altamiracorp.lumify.model.RowKeyHelper;
import com.altamiracorp.lumify.textExtraction.ArtifactExtractedInfo;
import com.altamiracorp.lumify.util.ThreadedTeeInputStreamWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

class HashCalculationWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalWorkData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HashCalculationWorker.class.getName());

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalWorkData additionalWorkData) throws Exception {
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
