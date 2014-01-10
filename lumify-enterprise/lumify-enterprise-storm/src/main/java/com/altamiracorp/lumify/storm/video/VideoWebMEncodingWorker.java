package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.video.VideoTextExtractionWorker;
import com.altamiracorp.lumify.core.model.ontology.DisplayType;
import com.altamiracorp.lumify.core.util.*;
import com.google.inject.Inject;

import java.io.InputStream;

import static org.mockito.internal.util.Checks.checkNotNull;

public class VideoWebMEncodingWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements VideoTextExtractionWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(VideoWebMEncodingWorker.class);
    private ProcessRunner processRunner;

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) {
    }

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.info("Encoding (webm) [VideoWebMEncodingWorker] %s", data.getFileName());
        HdfsLimitOutputStream out = new HdfsLimitOutputStream(data.getHdfsFileSystem(), 0);
        try {
            processRunner.execute(
                    "ffmpeg",
                    new String[]{
                            "-y", // overwrite output files
                            "-i", data.getLocalFileName(),
                            "-vcodec", "libvpx",
                            "-b:v", "600k",
                            "-qmin", "10",
                            "-qmax", "42",
                            "-maxrate", "500k",
                            "-bufsize", "1000k",
                            "-threads", "2",
                            "-vf", "scale=720:480",
                            "-acodec", "libvorbis",
                            "-map", "0", // process all streams
                            "-map", "-0:s", // ignore subtitles
                            "-f", "webm",
                            "-"
                    },
                    out,
                    data.getFileName() + ": "
            );
        } finally {
            out.close();
        }

        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        checkNotNull(out.getHdfsPath(), "hdfs path of output stream not correct");
        info.setWebMHdfsFilePath(out.getHdfsPath().toString());
        info.setConceptType(DisplayType.VIDEO.toString());
        LOGGER.debug("Finished [VideoWebMEncodingWorker]: %s", data.getFileName());
        return info;
    }

    @Inject
    public void setProcessRunner(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }
}
