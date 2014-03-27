package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.video.VideoTextExtractionWorker;
import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class TranscriptTextWorker
        extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>
        implements VideoTextExtractionWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TranscriptTextWorker.class);
    public static final String YOUTUBE_CC_FILE_NAME_SUFFIX = ".youtubecc";
    public static final String SRT_CC_FILE_NAME_SUFFIX = ".srt";

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Extracting transcripts [TranscriptTextWorker]: %s", data.getFileName());
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        VideoTranscript videoTranscript = new VideoTranscript();
        if (data.getArchiveTempDir() != null) {
            videoTranscript = readingTranscript(data);
            info.setTitle(FilenameUtils.getName(data.getFileName()).split(".lumify")[0]);
        }
        info.setVideoTranscript(videoTranscript);
        LOGGER.debug("Finished [TranscriptTextWorker]: %s", data.getFileName());
        return info;
    }

    private VideoTranscript readingTranscript(AdditionalArtifactWorkData data) throws Exception {
        File tempDir = data.getArchiveTempDir();
        checkNotNull(tempDir, "Video Transcripts must be in an archive file");
        checkState(tempDir.isDirectory(), "Archive temp directory not a directory");
        for (File f : tempDir.listFiles()) {
            if (!f.getName().startsWith(".")) {
                if (f.getName().endsWith(YOUTUBE_CC_FILE_NAME_SUFFIX)) {
                    return YoutubeccReader.read(f);
                } else if (f.getName().endsWith(SRT_CC_FILE_NAME_SUFFIX)) {
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
    public void prepare(TextExtractionWorkerPrepareData data) {
    }
}
