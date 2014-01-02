package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.video.VideoTextExtractionWorker;
import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ProcessRunner;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;

import java.io.File;
import java.io.InputStream;

public class CCExtractorWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements VideoTextExtractionWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(CCExtractorWorker.class);
    private ProcessRunner processRunner;

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) {
    }

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData additionalArtifactWorkData) throws Exception {
        LOGGER.debug("CCExtractor: %s", additionalArtifactWorkData.getFileName());
        File ccFile = File.createTempFile("ccextract", "txt");
        ccFile.delete();
        try {
            LOGGER.info("Extracting close captioning from: %s", additionalArtifactWorkData.getLocalFileName());
            processRunner.execute(
                    "ccextractor",
                    new String[]{
                            "-o", ccFile.getAbsolutePath(),
                            "-in=mp4",
                            additionalArtifactWorkData.getLocalFileName()
                    },
                    null,
                    additionalArtifactWorkData.getFileName() + ": "
            );

            VideoTranscript videoTranscript = SubRip.read(ccFile);
            ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
            artifactExtractedInfo.setVideoTranscript(videoTranscript);
            LOGGER.debug("Finished [CCExtractorWorker]: %s", additionalArtifactWorkData.getFileName());
            return artifactExtractedInfo;
        } finally {
            ccFile.delete();
        }
    }

    @Inject
    public void setProcessRunner(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }
}
