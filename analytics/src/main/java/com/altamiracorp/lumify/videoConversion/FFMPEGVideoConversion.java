package com.altamiracorp.lumify.videoConversion;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.util.StreamHelper;
import com.google.inject.Inject;
import org.apache.hadoop.thirdparty.guava.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class FFMPEGVideoConversion {
    private static final Logger LOGGER = LoggerFactory.getLogger(FFMPEGVideoConversion.class.getName());
    private ArtifactRepository artifactRepository;
    private VideoFrameRepository videoFrameRepository;

    @Inject
    public FFMPEGVideoConversion(ArtifactRepository artifactRepository, VideoFrameRepository videoFrameRepository) {
        this.artifactRepository = artifactRepository;
        this.videoFrameRepository = videoFrameRepository;
    }

    public void convert(Artifact artifact, User user) throws IOException, InterruptedException {
        File videoFile = writeFileToTemp(artifact, user);
        extractCloseCaptioning(videoFile, artifact, user);
        extractAudio(videoFile, artifact, user);
        encodeMp4(videoFile, artifact, user);
        encodeWebM(videoFile, artifact, user);
        extractPosterFrame(videoFile, artifact, user);
        extractFramesForAnalysis(videoFile, artifact, user);
        videoFile.delete();
    }

    private void extractAudio(File file, Artifact artifact, User user) throws IOException, InterruptedException {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        File audioFile = File.createTempFile("audio_", ".mp3");
//
//        // pass 1
//        LOGGER.info("Extracting audio from video " + file.getAbsolutePath() + " to " + audioFile.getAbsolutePath());
//        ffmpeg(new String[]{
//                "-i", file.getAbsolutePath(),
//                "-vn",
//                "-ar", "44100",
//                "-ab", "320k",
//                "-f", "mp3",
//                "-y",
//                audioFile.getAbsolutePath()
//        });
//
//        // save file
//        InputStream audioFileIn = new FileInputStream(audioFile);
//        SaveFileResults audioFileSaveResults = artifactRepository.saveFile(audioFileIn, user);
//        artifact.getGenericMetadata().setAudioHdfsFilePath(audioFileSaveResults.getFullPath());
//        audioFileIn.close();
//        audioFile.delete();
    }

    private void extractCloseCaptioning(File videoFile, Artifact artifact, User user) throws IOException, InterruptedException {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        File ccFile = File.createTempFile("ccextract", "txt");
//
//        LOGGER.info("Extracting close captioning from: " + videoFile.getAbsolutePath());
//        ccextractor(new String[]{
//                "-o", ccFile.getAbsolutePath(),
//                "-in=mp4",
//                videoFile.getAbsolutePath()
//        });
//
//        VideoTranscript videoTranscript = SubRip.read(ccFile);
//        artifact.getContent().mergeVideoTranscript(videoTranscript);
//
//        ccFile.delete();
    }

    private void extractFramesForAnalysis(File videoFile, Artifact artifact, User user) throws IOException, InterruptedException {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        Pattern fileNamePattern = Pattern.compile("image-([0-9]+)\\.png");
//        File tempDir = createTempDir("video-frames");
//
//        int framesPerSecondToExtract = 1;
//
//        LOGGER.info("Extracting video frames from: " + videoFile.getAbsolutePath());
//        ffmpeg(new String[]{
//                "-i", videoFile.getAbsolutePath(),
//                "-r", "" + framesPerSecondToExtract,
//                new File(tempDir, "image-%8d.png").getAbsolutePath()
//        });
//
//        long videoDuration = 0;
//        for (File frameFile : tempDir.listFiles()) {
//            Matcher m = fileNamePattern.matcher(frameFile.getName());
//            if (!m.matches()) {
//                continue;
//            }
//            long frameStartTime = (Long.parseLong(m.group(1)) / framesPerSecondToExtract) * 1000;
//            if (frameStartTime > videoDuration) {
//                videoDuration = frameStartTime;
//            }
//            FileInputStream frameIn = new FileInputStream(frameFile);
//            try {
//                videoFrameRepository.saveVideoFrame(artifact.getRowKey(), frameIn, frameStartTime, user);
//            } finally {
//                frameIn.close();
//            }
//        }
//        artifact.getContent().setVideoDuration(videoDuration);
//        FileUtils.deleteDirectory(tempDir);
    }

    private void extractPosterFrame(File file, Artifact artifact, User user) throws IOException, InterruptedException {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        File posterFrameFile = File.createTempFile("posterframe_", ".png");
//
//        // pass 1
//        LOGGER.info("Encoding (posterframe) " + file.getAbsolutePath() + " to " + posterFrameFile.getAbsolutePath());
//        ffmpeg(new String[]{
//                "-itsoffset", "-4",
//                "-i", file.getAbsolutePath(),
//                "-vcodec", "png",
//                "-vframes", "1",
//                "-an",
//                "-f", "rawvideo",
//                "-s", "720x480",
//                "-y",
//                posterFrameFile.getAbsolutePath()
//        });
//
//        // save file
//        InputStream posterFrameFileIn = new FileInputStream(posterFrameFile);
//        SaveFileResults posterFrameFileSaveResults = artifactRepository.saveFile(posterFrameFileIn, user);
//        artifact.getGenericMetadata().setPosterFrameHdfsFilePath(posterFrameFileSaveResults.getFullPath());
//        posterFrameFileIn.close();
//        posterFrameFile.delete();
    }

    private void encodeWebM(File file, Artifact artifact, User user) throws IOException, InterruptedException {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        File webmFile = File.createTempFile("encode_webm_", ".webm");
//
//        // pass 1
//        LOGGER.info("Encoding (webm) " + file.getAbsolutePath() + " to " + webmFile.getAbsolutePath());
//        ffmpeg(new String[]{
//                "-y", // overwrite output files
//                "-i", file.getAbsolutePath(),
//                "-vcodec", "libvpx",
//                "-b:v", "600k",
//                "-qmin", "10",
//                "-qmax", "42",
//                "-maxrate", "500k",
//                "-bufsize", "1000k",
//                "-threads", "2",
//                "-vf", "scale=720:480",
//                "-acodec", "libvorbis",
//                "-f", "webm",
//                webmFile.getAbsolutePath()
//        });
//
//        // save file
//        InputStream webmFileIn = new FileInputStream(webmFile);
//        SaveFileResults webmFileSaveResults = artifactRepository.saveFile(webmFileIn, user);
//        artifact.getGenericMetadata().setWebmHdfsFilePath(webmFileSaveResults.getFullPath());
//        webmFileIn.close();
//        webmFile.delete();
    }

    private void encodeMp4(File file, Artifact artifact, User user) throws IOException, InterruptedException {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        // encode mp4 file
//        File mp4File = File.createTempFile("encode_mp4_", ".mp4");
//        LOGGER.info("Encoding (mp4) " + file.getAbsolutePath() + " to " + mp4File.getAbsolutePath());
//        ffmpeg(new String[]{
//                "-y", // overwrite output files
//                "-i", file.getAbsolutePath(),
//                "-vcodec", "libx264",
//                "-vprofile", "high",
//                "-preset", "slow",
//                "-b:v", "500k",
//                "-maxrate", "500k",
//                "-bufsize", "1000k",
//                "-vf", "scale=720:480",
//                "-threads", "0",
//                "-acodec", "libfdk_aac",
//                "-b:a", "128k",
//                "-f", "mp4",
//                mp4File.getAbsolutePath()
//        });
//
//        // relocate metadata
//        File mp4ReloactedFile = File.createTempFile("encode_mp4_relocate_", ".mp4");
//        qtFaststart(new String[]{
//                mp4File.getAbsolutePath(),
//                mp4ReloactedFile.getAbsolutePath()
//        });
//        mp4File.delete();
//
//        // save file
//        InputStream mp4ReloactedFileIn = new FileInputStream(mp4ReloactedFile);
//        SaveFileResults mp4FileSaveResults = artifactRepository.saveFile(mp4ReloactedFileIn, user);
//        artifact.getGenericMetadata().setMp4HdfsFilePath(mp4FileSaveResults.getFullPath());
//        mp4ReloactedFileIn.close();
//        mp4ReloactedFile.delete();
    }

    private void qtFaststart(String[] args) throws IOException, InterruptedException {
        executeProgram("qt-faststart", args);
    }

    private void ffmpeg(String[] args) throws IOException, InterruptedException {
        executeProgram("ffmpeg", args);
    }

    private void ccextractor(String[] args) throws IOException, InterruptedException {
        executeProgram("ccextractor", args);
    }

    private void executeProgram(final String programName, final String[] programArgs) throws IOException, InterruptedException {
        final List<String> arguments = Lists.newArrayList(programName);
        arguments.addAll(Arrays.asList(programArgs));

        final ProcessBuilder procBuilder = new ProcessBuilder(arguments);
        final Map<String, String> sortedEnv = new TreeMap<String, String>(procBuilder.environment());

        LOGGER.info("Running: " + arrayToString(arguments));

        if (!sortedEnv.isEmpty()) {
            LOGGER.info("Spawned program environment: ");
            for (final Map.Entry<String, String> entry : sortedEnv.entrySet()) {
                LOGGER.info(String.format("%s:%s", entry.getKey(), entry.getValue()));
            }
        } else {
            LOGGER.info("Running program environment is empty");
        }

        final Process proc = procBuilder.start();

        final int returnCode = runProc(proc, programName);
        if (returnCode != 0) {
            throw new RuntimeException("unexpected return code: " + returnCode + " for command " + arrayToString(arguments));
        }
    }

    private int runProc(Process proc, String logPrefix) throws InterruptedException, IOException {
        StreamHelper inStreamHelper = new StreamHelper(proc.getInputStream(), LOGGER, logPrefix + "(stdout): ");
        inStreamHelper.start();

        StreamHelper errStreamHelper = new StreamHelper(proc.getErrorStream(), LOGGER, logPrefix + "(stderr): ");
        errStreamHelper.start();

        proc.waitFor();

        synchronized (inStreamHelper) {
            inStreamHelper.join(10000);
        }

        synchronized (errStreamHelper) {
            errStreamHelper.join(10000);
        }

        LOGGER.info(logPrefix + "(returncode): " + proc.exitValue());

        return proc.exitValue();
    }

    private String arrayToString(List<String> arr) {
        StringBuilder result = new StringBuilder();
        for (String s : arr) {
            result.append(s).append(' ');
        }
        return result.toString();
    }

    private File writeFileToTemp(Artifact artifact, User user) throws IOException {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        File tempFile = File.createTempFile("video_", "." + artifact.getGenericMetadata().getFileExtension());
//        InputStream in = artifactRepository.getRaw(artifact, user);
//        try {
//            FileOutputStream out = new FileOutputStream(tempFile);
//            try {
//                IOUtils.copy(in, out);
//                out.flush();
//            } finally {
//                out.close();
//            }
//        } finally {
//            in.close();
//        }
//        return tempFile;
    }

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

    public ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }

    public void setArtifactRepository(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }
}
