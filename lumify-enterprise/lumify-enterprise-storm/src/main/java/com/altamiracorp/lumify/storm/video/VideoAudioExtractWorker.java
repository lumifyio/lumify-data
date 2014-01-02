package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.video.VideoTextExtractionWorker;
import com.altamiracorp.lumify.core.util.*;
import com.google.inject.Inject;

import java.io.InputStream;

public class VideoAudioExtractWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements VideoTextExtractionWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(VideoAudioExtractWorker.class);
    private ProcessRunner processRunner;

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) {
    }

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData additionalArtifactWorkData) throws Exception {
        LOGGER.debug("Extracting Video Audio [VideoAudioExtractWorker]: %s", additionalArtifactWorkData.getFileName());
        LOGGER.info("Extracting audio from video %s", additionalArtifactWorkData.getLocalFileName());
        HdfsLimitOutputStream out = new HdfsLimitOutputStream(additionalArtifactWorkData.getHdfsFileSystem(), 0);
        try {
            processRunner.execute(
                    "ffmpeg",
                    new String[]{
                            "-i", additionalArtifactWorkData.getLocalFileName(),
                            "-vn",
                            "-ar", "44100",
                            "-ab", "320k",
                            "-f", "mp3",
                            "-y",
                            "-"
                    },
                    out,
                    additionalArtifactWorkData.getFileName() + ": "
            );
        } finally {
            out.close();
        }

        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        info.setAudioHdfsPath(out.getHdfsPath().toString());
        LOGGER.debug("Finished [VideoAudioExtractWorker]: %s", additionalArtifactWorkData.getFileName());
        return info;
    }

    @Inject
    public void setProcessRunner(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }
}
