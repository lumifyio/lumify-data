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

public class VideoAudioExtractWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements VideoTextExtractionWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoAudioExtractWorker.class);

    @Override
    public void prepare(Map stormConf, User user) {
    }

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData additionalArtifactWorkData) throws Exception {
        LOGGER.info("Extracting audio from video " + additionalArtifactWorkData.getLocalFileName());
        HdfsLimitOutputStream out = new HdfsLimitOutputStream(additionalArtifactWorkData.getHdfsFileSystem(), 0);
        try {
            ProcessRunner.execute(
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
                    out);
        } finally {
            out.close();
        }

        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        info.setAudioHdfsPath(out.getHdfsPath().toString());
        return info;
    }
}
