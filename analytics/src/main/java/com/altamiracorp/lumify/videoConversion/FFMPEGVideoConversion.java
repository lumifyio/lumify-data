package com.altamiracorp.lumify.videoConversion;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.core.util.StreamHelper;
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
        encodeMp4(videoFile, artifact, user);
        videoFile.delete();
    }

    private void encodeMp4(File file, Artifact artifact, User user) throws IOException, InterruptedException {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        // encode mp4 file
//        LOGGER.info("Encoding (mp4) " + file.getAbsolutePath() + " to " + mp4File.getAbsolutePath());
//
//        // relocate metadata
//        File mp4ReloactedFile = File.createTempFile("encode_mp4_relocate_", ".mp4");
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

    public ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }

    public void setArtifactRepository(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }
}
