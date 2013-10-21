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

public class VideoWebMEncodingWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements VideoTextExtractionWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoWebMEncodingWorker.class);

    @Override
    public void prepare(Map stormConf, User user) {
    }

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.info("Encoding (webm) " + data.getFileName());
        HdfsLimitOutputStream out = new HdfsLimitOutputStream(data.getHdfsFileSystem(), 0);
        try {
            ProcessRunner.execute(
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
                    out
            );
        } finally {
            out.close();
        }

        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        info.setWebMHdfsFilePath(out.getHdfsPath().toString());

        return info;
    }
}
