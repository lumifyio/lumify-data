package com.altamiracorp.reddawn.videoConversion;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.SaveFileResults;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.artifact.VideoTranscript;
import com.altamiracorp.reddawn.util.StreamHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FFMPEGVideoConversion {
    private static final Logger LOGGER = LoggerFactory.getLogger(FFMPEGVideoConversion.class.getName());
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private VideoFrameRepository videoFrameRepository = new VideoFrameRepository();

    public void convert(RedDawnSession session, Artifact artifact) throws IOException, InterruptedException {
        File videoFile = writeFileToTemp(session, artifact);
        extractCloseCaptioning(session, videoFile, artifact);
        extractAudio(session, videoFile, artifact);
        encodeMp4(session, videoFile, artifact);
        encodeWebM(session, videoFile, artifact);
        extractPosterFrame(session, videoFile, artifact);
        extractFramesForAnalysis(session, videoFile, artifact);
        videoFile.delete();
    }

    private void extractAudio(RedDawnSession session, File file, Artifact artifact) throws IOException, InterruptedException {
        File audioFile = File.createTempFile("audio_", ".mp3");

        // pass 1
        LOGGER.info("Extracting audio from video " + file.getAbsolutePath() + " to " + audioFile.getAbsolutePath());
        ffmpeg(new String[]{
                "-i", file.getAbsolutePath(),
                "-vn",
                //"-ac", "2",
                "-ar", "44100",
                "-ab", "320k",
                "-f", "mp3",
                "-y",
                audioFile.getAbsolutePath()
        });

        // save file
        InputStream audioFileIn = new FileInputStream(audioFile);
        SaveFileResults audioFileSaveResults = artifactRepository.saveFile(session.getModelSession(), audioFileIn);
        artifact.getGenericMetadata().setAudioHdfsFilePath(audioFileSaveResults.getFullPath());
        audioFileIn.close();
        audioFile.delete();
    }

    private void extractCloseCaptioning(RedDawnSession session, File videoFile, Artifact artifact) throws IOException, InterruptedException {
        File ccFile = File.createTempFile("ccextract", "txt");

        LOGGER.info("Extracting close captioning from: " + videoFile.getAbsolutePath());
        ccextractor(new String[]{
                "-o", ccFile.getAbsolutePath(),
                "-in=mp4",
                videoFile.getAbsolutePath()
        });

        VideoTranscript videoTranscript = SubRip.read(ccFile);
        artifact.getContent().setVideoTranscript(videoTranscript);

        ccFile.delete();
    }

    private void extractFramesForAnalysis(RedDawnSession session, File videoFile, Artifact artifact) throws IOException, InterruptedException {
        Pattern fileNamePattern = Pattern.compile("image-([0-9]+)\\.png");
        File tempDir = createTempDir("video-frames");

        int framesPerSecondToExtract = 1;

        LOGGER.info("Extracting video frames from: " + videoFile.getAbsolutePath());
        ffmpeg(new String[]{
                "-i", videoFile.getAbsolutePath(),
                "-r", "" + framesPerSecondToExtract,
                new File(tempDir, "image-%8d.png").getAbsolutePath()
        });

        for (File frameFile : tempDir.listFiles()) {
            Matcher m = fileNamePattern.matcher(frameFile.getName());
            if (!m.matches()) {
                continue;
            }
            long frameStartTime = (Long.parseLong(m.group(1)) / framesPerSecondToExtract) * 1000;
            FileInputStream frameIn = new FileInputStream(frameFile);
            try {
                videoFrameRepository.saveVideoFrame(session.getModelSession(), artifact.getRowKey(), frameIn, frameStartTime);
            } finally {
                frameIn.close();
            }
        }
        FileUtils.deleteDirectory(tempDir);
    }

    private void extractPosterFrame(RedDawnSession session, File file, Artifact artifact) throws IOException, InterruptedException {
        File posterFrameFile = File.createTempFile("posterframe_", ".png");

        // pass 1
        LOGGER.info("Encoding (posterframe) " + file.getAbsolutePath() + " to " + posterFrameFile.getAbsolutePath());
        ffmpeg(new String[]{
                "-itsoffset", "-4",
                "-i", file.getAbsolutePath(),
                "-vcodec", "png",
                "-vframes", "1",
                "-an",
                "-f", "rawvideo",
                "-s", "720x480",
                "-y",
                posterFrameFile.getAbsolutePath()
        });

        // save file
        InputStream posterFrameFileIn = new FileInputStream(posterFrameFile);
        SaveFileResults posterFrameFileSaveResults = artifactRepository.saveFile(session.getModelSession(), posterFrameFileIn);
        artifact.getGenericMetadata().setPosterFrameHdfsFilePath(posterFrameFileSaveResults.getFullPath());
        posterFrameFileIn.close();
        posterFrameFile.delete();
    }

    private void encodeWebM(RedDawnSession session, File file, Artifact artifact) throws IOException, InterruptedException {
        File webmFile = File.createTempFile("encode_webm_", ".webm");

        // pass 1
        LOGGER.info("Encoding (webm) " + file.getAbsolutePath() + " to " + webmFile.getAbsolutePath());
        ffmpeg(new String[]{
                "-y", // overwrite output files
                "-i", file.getAbsolutePath(),
                "-vcodec", "libvpx",
                "-b:v", "600k",
                "-qmin", "10",
                "-qmax", "42",
                "-maxrate", "500k",
                "-bufsize", "1000k",
                "-threads", "2",
                "-vf", "scale=720:480",
                "-acodec", "libvorbis",
                "-b:a", "128k",
                "-f", "webm",
                webmFile.getAbsolutePath()
        });

        // save file
        InputStream webmFileIn = new FileInputStream(webmFile);
        SaveFileResults webmFileSaveResults = artifactRepository.saveFile(session.getModelSession(), webmFileIn);
        artifact.getGenericMetadata().setWebmHdfsFilePath(webmFileSaveResults.getFullPath());
        webmFileIn.close();
        //webmFile.delete();
    }

    private void encodeMp4(RedDawnSession session, File file, Artifact artifact) throws IOException, InterruptedException {
        // encode mp4 file
        File mp4File = File.createTempFile("encode_mp4_", ".mp4");
        LOGGER.info("Encoding (mp4) " + file.getAbsolutePath() + " to " + mp4File.getAbsolutePath());
        ffmpeg(new String[]{
                "-y", // overwrite output files
                "-i", file.getAbsolutePath(),
                "-vcodec", "libx264",
                "-vprofile", "high",
                "-preset", "slow",
                "-b:v", "500k",
                "-maxrate", "500k",
                "-bufsize", "1000k",
                "-vf", "scale=720:480",
                "-threads", "0",
                "-acodec", "libvo_aacenc",
                "-b:a", "128k",
                "-f", "mp4",
                mp4File.getAbsolutePath()
        });

        // relocate metadata
        File mp4ReloactedFile = File.createTempFile("encode_mp4_relocate_", ".mp4");
        qtFaststart(new String[]{
                mp4File.getAbsolutePath(),
                mp4ReloactedFile.getAbsolutePath()
        });
        mp4File.delete();

        // save file
        InputStream mp4ReloactedFileIn = new FileInputStream(mp4ReloactedFile);
        SaveFileResults mp4FileSaveResults = artifactRepository.saveFile(session.getModelSession(), mp4ReloactedFileIn);
        artifact.getGenericMetadata().setMp4HdfsFilePath(mp4FileSaveResults.getFullPath());
        mp4ReloactedFileIn.close();
        mp4ReloactedFile.delete();
    }

    private void qtFaststart(String[] args) throws IOException, InterruptedException {
        ArrayList<String> ffmpegArgs = new ArrayList<String>();
        ffmpegArgs.add("qt-faststart");
        for (String arg : args) {
            ffmpegArgs.add(arg);
        }
        ProcessBuilder procBuilder = new ProcessBuilder(ffmpegArgs);
        LOGGER.info("Running: " + arrayToString(ffmpegArgs));
        Process proc = procBuilder.start();
        int returnCode = runProc(proc, "qt-faststart");
        if (returnCode != 0) {
            throw new RuntimeException("unexpected return code: " + returnCode + " for command " + arrayToString(ffmpegArgs));
        }
    }

    private void ffmpeg(String[] args) throws IOException, InterruptedException {
        ArrayList<String> ffmpegArgs = new ArrayList<String>();
        ffmpegArgs.add("ffmpeg");
        for (String arg : args) {
            ffmpegArgs.add(arg);
        }
        ProcessBuilder procBuilder = new ProcessBuilder(ffmpegArgs);
        LOGGER.info("Running: " + arrayToString(ffmpegArgs));
        Process proc = procBuilder.start();
        int returnCode = runProc(proc, "ffmpeg");
        if (returnCode != 0) {
            throw new RuntimeException("unexpected return code: " + returnCode + " for command " + arrayToString(ffmpegArgs));
        }
    }

    private void ccextractor(String[] args) throws IOException, InterruptedException {
        ArrayList<String> ffmpegArgs = new ArrayList<String>();
        ffmpegArgs.add("ccextractor");
        for (String arg : args) {
            ffmpegArgs.add(arg);
        }
        ProcessBuilder procBuilder = new ProcessBuilder(ffmpegArgs);
        LOGGER.info("Running: " + arrayToString(ffmpegArgs));
        Process proc = procBuilder.start();
        int returnCode = runProc(proc, "ccextractor");
        if (returnCode != 0) {
            throw new RuntimeException("unexpected return code: " + returnCode + " for command " + arrayToString(ffmpegArgs));
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

    private String arrayToString(ArrayList<String> arr) {
        StringBuilder result = new StringBuilder();
        for (String s : arr) {
            result.append(s).append(' ');
        }
        return result.toString();
    }

    private File writeFileToTemp(RedDawnSession session, Artifact artifact) throws IOException {
        File tempFile = File.createTempFile("video_", "." + artifact.getGenericMetadata().getFileExtension());
        InputStream in = artifactRepository.getRaw(session.getModelSession(), artifact);
        try {
            FileOutputStream out = new FileOutputStream(tempFile);
            try {
                IOUtils.copy(in, out);
                out.flush();
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
        return tempFile;
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
