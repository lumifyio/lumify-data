package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.video.VideoTextExtractionWorker;
import com.altamiracorp.lumify.core.util.*;
import com.google.inject.Inject;

import java.io.File;
import java.io.InputStream;

public class VideoPosterFrameWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements VideoTextExtractionWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(VideoPosterFrameWorker.class);
    private ProcessRunner processRunner;

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) {
    }

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData additionalArtifactWorkData) throws Exception {
        File inputFile = new File(additionalArtifactWorkData.getLocalFileName());
        LOGGER.info("Encoding (posterframe) [VideoPosterFrameWorker] %s, length: %d", inputFile.getAbsolutePath(), inputFile.length());
        HdfsLimitOutputStream out = new HdfsLimitOutputStream(additionalArtifactWorkData.getHdfsFileSystem(), 0);
        try {
            processRunner.execute(
                    "ffmpeg",
                    new String[]{
                            "-itsoffset", "-4",
                            "-i", additionalArtifactWorkData.getLocalFileName(),
                            "-vcodec", "png",
                            "-vframes", "1",
                            "-an",
                            "-f", "rawvideo",
                            "-s", "720x480",
                            "-y",
                            "-"
                    },
                    out,
                    additionalArtifactWorkData.getLocalFileName() + ": "
            );
        } finally {
            out.close();
        }

        if (out.getLength() == 0) {
            throw new RuntimeException("Poster frame not created. Zero length file detected. (from: " + inputFile.getAbsolutePath() + ", length: " + inputFile.length() + ")");
        }

        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        info.setPosterFrameHdfsPath(out.getHdfsPath().toString());
        LOGGER.debug("Finished [VideoPosterFrameWorker]: %s", additionalArtifactWorkData.getFileName());
        return info;
    }

    @Inject
    public void setProcessRunner(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }
}
