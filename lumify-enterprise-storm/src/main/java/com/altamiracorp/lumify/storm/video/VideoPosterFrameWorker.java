package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.video.VideoTextExtractionWorker;
import com.altamiracorp.lumify.core.util.HdfsLimitOutputStream;
import com.altamiracorp.lumify.core.util.ProcessRunner;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

public class VideoPosterFrameWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements VideoTextExtractionWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoPosterFrameWorker.class);
    private ProcessRunner processRunner;

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) {
    }

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData additionalArtifactWorkData) throws Exception {
        File inputFile = new File(additionalArtifactWorkData.getLocalFileName());
        LOGGER.info("Encoding (posterframe) [VideoPosterFrameWorker] " + inputFile.getAbsolutePath() + ", length: " + inputFile.length());
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
        LOGGER.debug("Finished [VideoPosterFrameWorker]: " + additionalArtifactWorkData.getFileName());
        return info;
    }

    @Inject
    public void setProcessRunner(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }
}
