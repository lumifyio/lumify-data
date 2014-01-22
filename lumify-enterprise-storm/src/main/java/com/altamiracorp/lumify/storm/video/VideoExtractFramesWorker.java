package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.video.VideoTextExtractionWorker;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ProcessRunner;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoExtractFramesWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements VideoTextExtractionWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(VideoExtractFramesWorker.class);
    private static final Random random = new Random();

    private VideoFrameTextExtractor videoFrameTextExtractor;
    private ProcessRunner processRunner;

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) {
    }

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData additionalArtifactWorkData) throws Exception {
        LOGGER.debug("Extracting Frames [VideoExtractFramesWorker]: %s", additionalArtifactWorkData.getFileName());
        Pattern fileNamePattern = Pattern.compile("image-([0-9]+)\\.png");
        File tempDir = createTempDir("video-frames");

        int framesPerSecondToExtract = 1;

        LOGGER.info("Extracting video frames from: %s", additionalArtifactWorkData.getLocalFileName());
        processRunner.execute(
                "ffmpeg",
                new String[]{
                        "-i", additionalArtifactWorkData.getLocalFileName(),
                        "-r", "" + framesPerSecondToExtract,
                        new File(tempDir, "image-%8d.png").getAbsolutePath()
                },
                null,
                additionalArtifactWorkData.getFileName() + ": "
        );

        long videoDuration = 0;
        ArrayList<ArtifactExtractedInfo.VideoFrame> videoFrames = new ArrayList<ArtifactExtractedInfo.VideoFrame>();
        int id = Math.abs(random.nextInt());
        for (File frameFile : tempDir.listFiles()) {
            Matcher m = fileNamePattern.matcher(frameFile.getName());
            if (!m.matches()) {
                continue;
            }
            long frameStartTime = (Long.parseLong(m.group(1)) / framesPerSecondToExtract) * 1000;
            if (frameStartTime > videoDuration) {
                videoDuration = frameStartTime;
            }

            Path destPath = new Path("/tmp/videoExtractFrame-" + id + "-" + frameStartTime + ".png");
            additionalArtifactWorkData.getHdfsFileSystem().copyFromLocalFile(true, true, new Path(frameFile.getAbsolutePath()), destPath);
            videoFrames.add(new ArtifactExtractedInfo.VideoFrame(destPath.toString(), frameStartTime));
        }
        FileUtils.deleteDirectory(tempDir);

        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        info.setVideoDuration(videoDuration);
        info.setVideoFrames(videoFrames);

        String text = videoFrameTextExtractor.extract(videoFrames, additionalArtifactWorkData).getText();
        info.setText(text);

        LOGGER.debug("Finished [VideoExtractFramesWorker]: %s", additionalArtifactWorkData.getFileName());
        return info;
    }

    // TODO refactor into a helper class
    private File createTempDir(String prefix) {
        int tempDirAttempts = 10000;
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = prefix + "-" + System.currentTimeMillis() + "-";

        for (int counter = 0; counter < tempDirAttempts; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException("Failed to create directory within "
                + tempDirAttempts + " attempts (tried "
                + baseName + "0 to " + baseName + (tempDirAttempts - 1) + ')');
    }

    @Inject
    public void setVideoFrameTextExtractor(VideoFrameTextExtractor videoFrameTextExtractor) {
        this.videoFrameTextExtractor = videoFrameTextExtractor;
    }

    @Inject
    public void setProcessRunner(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }
}
