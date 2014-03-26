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
    private static final long BYTES_PER_SAMPLE = 2;
    private static final long SAMPLES_PER_SECOND = 16000;
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
        File wavFileNoSilence = File.createTempFile("encode_wav_no_silence_", ".wav");
        File wavFileNoHeaders = File.createTempFile("encode_wav_noheader_", ".wav");

        try {
            processRunner.execute(
                    "ffmpeg",
                    new String[]{
                            "-y", // overwrite output files
                            "-i", data.getLocalFileName(),
                            "-acodec", "pcm_s16le",
                            "-ac", "1",
                            "-ar", Long.toString(SAMPLES_PER_SECOND),
                            wavFile.getAbsolutePath()
                    },
                    null,
                    data.getFileName() + ": "
            );

            processRunner.execute(
                    "sox",
                    new String[]{
                            wavFile.getAbsolutePath(),
                            wavFileNoSilence.getAbsolutePath(),
                            "silence", "1", "0.1", "1%", // remove silence from beginning. at least 0.1s of less than 1% volume
                            "pad", "1", "0" // pad 1 second of silence to beginning
                    },
                    null,
                    data.getFileName() + ": "
            );

            long silenceFileSizeDiff = wavFile.length() - wavFileNoSilence.length();
            double timeOffsetInSec = (double) silenceFileSizeDiff / BYTES_PER_SAMPLE / SAMPLES_PER_SECOND;

            // TODO patch sphinx to handle headers correctly
            fixWavHeaders(wavFileNoSilence, wavFileNoHeaders);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                processRunner.execute(
                        "pocketsphinx_continuous",
                        new String[]{
                                "-infile", wavFileNoHeaders.getAbsolutePath(),
                                "-time", "true"
                        },
                        out,
                        data.getFileName() + ": "
                );
            } finally {
                out.close();
            }

            return parseSphinxOutput(new String(out.toByteArray()), timeOffsetInSec);
        } finally {
            wavFile.delete();
            wavFileNoSilence.delete();
            wavFileNoHeaders.delete();
        }
    }

    // see https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
    private void fixWavHeaders(File wavFile, File wavFileNoHeaders) throws IOException {
        byte[] buffer = new byte[1024];
        InputStream in = new FileInputStream(wavFile);
        OutputStream out = new FileOutputStream(wavFileNoHeaders);
        try {
            int read;

            // read RIFF head
            read = in.read(buffer, 0, 12);
            if (read < 12) {
                throw new IOException("Could not read RIFF header");
            }
            out.write(buffer, 0, 12);

            // skip non-data subchunks
            while (true) {
                read = in.read(buffer, 0, 8);
                if (read < 8) {
                    throw new IOException("Could not read subchunk");
                }
                String subchunkName = new String(buffer, 0, 4);
                if (subchunkName.equals("data")) {
                    out.write(buffer, 0, 8);
                    break;
                }
                int chunkSize = ((((int) buffer[4]) << 0) | (((int) buffer[5]) << 8) | (((int) buffer[6]) << 16) | (((int) buffer[7]) << 24));
                while (chunkSize > 0) {
                    read = Math.min(chunkSize, buffer.length);
                    in.read(buffer, 0, read);
                    chunkSize -= read;
                }
            }

            // copy remaining data
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    private VideoTranscript parseSphinxOutput(String output, double offsetInSec) throws IOException {
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
                    long s = (long) ((sentenceStartTime + offsetInSec) * 1000);
                    long e = (long) ((endTime + offsetInSec) * 1000);
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
