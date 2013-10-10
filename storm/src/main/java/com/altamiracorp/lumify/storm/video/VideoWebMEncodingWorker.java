package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.video.VideoTextExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.HdfsLimitOutputStream;
import com.altamiracorp.lumify.core.util.Pipe;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.util.StreamHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.thirdparty.guava.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class VideoWebMEncodingWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements VideoTextExtractionWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoWebMEncodingWorker.class);

    @Override
    public void prepare(Map stormConf, User user) {
    }

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.info("Encoding (webm) " + data.getFileName());

        File tempFile = File.createTempFile("webmencoding-", FilenameUtils.getExtension(data.getFileName()));
        try {
            OutputStream tempFileOut = new FileOutputStream(tempFile);
            IOUtils.copy(work, tempFileOut);

            HdfsLimitOutputStream out = new HdfsLimitOutputStream(data.getHdfsFileSystem(), 0);
            try {
                ffmpeg(
                        new String[]{
                                "-y", // overwrite output files
                                "-i", tempFile.getAbsolutePath(),
                                "-vcodec", "libvpx",
                                "-b:v", "600k",
                                "-qmin", "10",
                                "-qmax", "42",
                                "-maxrate", "500k",
                                "-bufsize", "1000k",
                                "-threads", "2",
                                "-vf", "scale=720:480",
                                "-acodec", "libvorbis",
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
        } finally {
            tempFile.delete();
        }
    }

    private void ffmpeg(String[] args, OutputStream out) throws IOException, InterruptedException {
        executeProgram("ffmpeg", args, out);
    }

    private void executeProgram(final String programName, final String[] programArgs, OutputStream out) throws IOException, InterruptedException {
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

        StreamHelper errStreamHelper = new StreamHelper(proc.getErrorStream(), LOGGER, programName + "(stderr): ");
        errStreamHelper.start();

        final IOException[] pipeException = new IOException[1];
        Pipe.ExceptionHandler exceptionHandler = new Pipe.ExceptionHandler() {
            @Override
            public void handle(IOException e) {
                pipeException[0] = e;
            }
        };
        Pipe.pipe(proc.getInputStream(), out, exceptionHandler);

        proc.waitFor();

        errStreamHelper.join(10000);

        LOGGER.info(programName + "(returncode): " + proc.exitValue());

        if (proc.exitValue() != 0) {
            throw new RuntimeException("unexpected return code: " + proc.exitValue() + " for command " + arrayToString(arguments));
        }
        if (pipeException[0] != null) {
            throw new RuntimeException("pipe exception", pipeException[0]);
        }
    }

    private String arrayToString(List<String> arr) {
        StringBuilder result = new StringBuilder();
        for (String s : arr) {
            result.append(s).append(' ');
        }
        return result.toString();
    }
}
