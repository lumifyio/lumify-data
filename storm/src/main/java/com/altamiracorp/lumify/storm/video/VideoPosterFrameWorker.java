package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.video.VideoTextExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.HdfsLimitOutputStream;
import com.altamiracorp.lumify.core.util.ProcessRunner;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;

public class VideoPosterFrameWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements VideoTextExtractionWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoPosterFrameWorker.class);

    @Override
    public void prepare(Map stormConf, User user) {
    }

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData additionalArtifactWorkData) throws Exception {
        LOGGER.info("Encoding (posterframe) " + additionalArtifactWorkData.getLocalFileName());
        HdfsLimitOutputStream out = new HdfsLimitOutputStream(additionalArtifactWorkData.getHdfsFileSystem(), 0);
        try {
            ProcessRunner.execute(
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
                    out);
        } finally {
            out.close();
        }

        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        info.setPosterFrameHdfsPath(out.getHdfsPath().toString());
        return info;
    }
}
