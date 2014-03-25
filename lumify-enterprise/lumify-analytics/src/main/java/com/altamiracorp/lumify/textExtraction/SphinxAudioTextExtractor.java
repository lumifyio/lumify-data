package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ProcessRunner;
import com.google.inject.Inject;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SphinxAudioTextExtractor {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(SphinxAudioTextExtractor.class);
    private ProcessRunner processRunner;

    public ArtifactExtractedInfo extractFromAudio(InputStream work, AdditionalArtifactWorkData data) throws IOException, InterruptedException {
        VideoTranscript transcript = extractTranscriptFromAudio(work, data);
        if (transcript == null) {
            return null;
        }

        ArtifactExtractedInfo extractedInfo = new ArtifactExtractedInfo();
        extractedInfo.setVideoTranscript(transcript);
        extractedInfo.setVideoDuration(transcript.getDuration());
        return extractedInfo;
    }

    private VideoTranscript extractTranscriptFromAudio(InputStream work, AdditionalArtifactWorkData data) throws IOException, InterruptedException {
        File wavFile = File.createTempFile("encode_wav_", ".wav");

        try {
            processRunner.execute(
                    "ffmpeg",
                    new String[]{
                            "-y", // overwrite output files
                            "-i", data.getLocalFileName(),
                            "-acodec", "pcm_s16le",
                            "-ac", "1",
                            "-ar", "16000",
                            wavFile.getAbsolutePath()
                    },
                    null,
                    data.getFileName() + ": "
            );

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                processRunner.execute(
                        "pocketsphinx_continuous",
                        new String[]{
                                "-infile", wavFile.getAbsolutePath(),
                                "-time", "true"
                        },
                        out,
                        data.getFileName() + ": "
                );
            } finally {
                out.close();
            }

            return parseSphinxOutput(new String(out.toByteArray()));
        } finally {
            wavFile.delete();
        }
    }

    private VideoTranscript parseSphinxOutput(String output) throws IOException {
        VideoTranscript transcript = new VideoTranscript();
        BufferedReader reader = new BufferedReader(new StringReader(output));

        Pattern wordPattern = Pattern.compile("^([^\\s]+) ([0-9\\.]+) ([0-9\\.]+) ([0-9\\.]+)$");
        String line;
        StringBuilder sentence = new StringBuilder();
        double sentenceStartTime = 0.0;
        while ((line = reader.readLine()) != null) {
            Matcher m = wordPattern.matcher(line);
            if (m.matches()) {
                String word = m.group(1);
                double startTime = Double.parseDouble(m.group(2));
                double endTime = Double.parseDouble(m.group(2));
                double duration = Double.parseDouble(m.group(2));
                if ("<s>".equals(word)) {
                    sentence = new StringBuilder();
                    sentenceStartTime = startTime;
                } else if ("</s>".equals(word)) {
                    long s = (long) (sentenceStartTime * 1000);
                    long e = (long) (endTime * 1000);
                    transcript.add(new VideoTranscript.Time(s, e), sentence.toString().trim());
                    sentence = new StringBuilder();
                } else {
                    sentence.append(word);
                    sentence.append(' ');
                }
            }
        }

        return transcript;
    }

    @Inject
    public void setProcessRunner(ProcessRunner ffmpeg) {
        this.processRunner = ffmpeg;
    }
}
