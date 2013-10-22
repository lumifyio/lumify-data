package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.video.VideoTextExtractionWorker;
import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class TranscriptTextWorker
        extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>
        implements VideoTextExtractionWorker {

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {

        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        VideoTranscript videoTranscript = new VideoTranscript();
        if (data.getArchiveTempDir() != null) {
            videoTranscript = readingTranscript(data);
        }
        info.setVideoTranscript(videoTranscript);

        return info;
    }

    private VideoTranscript readingTranscript(AdditionalArtifactWorkData data) throws Exception {
        File tempDir = data.getArchiveTempDir();
        checkNotNull(tempDir, "Video Transcripts must be in an archive file");
        checkState(tempDir.isDirectory(), "Archive temp directory not a directory");
        for (File f : tempDir.listFiles()) {
            if (!f.getName().startsWith(".")) {
                if (f.getName().endsWith(VideoContentTypeSorter.YOUTUBE_CC_FILE_NAME_SUFFIX)) {
                    return YoutubeccReader.read(f);
                } else if (f.getName().endsWith(VideoContentTypeSorter.SRT_CC_FILE_NAME_SUFFIX)) {
                    return SubRip.read(f);
                }
            }
        }
        throw new RuntimeException("Could not find supported transcript file file in directory: " + tempDir);
    }

    public String getName() {
        return "transcriptTextWorker";
    }

    //    @Override
    public void prepare(Map stormConf, User user) {
    }
}
