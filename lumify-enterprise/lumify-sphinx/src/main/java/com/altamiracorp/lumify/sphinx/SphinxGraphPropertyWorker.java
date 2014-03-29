package com.altamiracorp.lumify.sphinx;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkResult;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import com.altamiracorp.lumify.core.model.properties.MediaLumifyProperties;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.util.ProcessRunner;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.mutation.ExistingElementMutation;
import com.google.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SphinxGraphPropertyWorker extends GraphPropertyWorker {
    private static final long BYTES_PER_SAMPLE = 2;
    private static final long SAMPLES_PER_SECOND = 16000;
    public static final String MULTI_VALUE_KEY = SphinxGraphPropertyWorker.class.getName();
    private ProcessRunner processRunner;

    @Override
    public GraphPropertyWorkResult execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        VideoTranscript transcript = extractTranscriptFromAudio(data.getLocalFile());
        if (transcript == null) {
            return null;
        }

        ExistingElementMutation<Vertex> m = data.getVertex().prepareMutation();
        MediaLumifyProperties.VIDEO_TRANSCRIPT.addPropertyValue(m, MULTI_VALUE_KEY, transcript.toJson().toString(), data.getVertex().getVisibility());
        m.save();

        getGraph().flush();
        getWorkQueueRepository().pushGraphPropertyQueue(data.getVertex().getId(), MULTI_VALUE_KEY, MediaLumifyProperties.VIDEO_TRANSCRIPT.getKey());

        return new GraphPropertyWorkResult();
    }

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.MIME_TYPE.getKey());
        return !(mimeType == null || !mimeType.startsWith("audio"));
    }

    private VideoTranscript extractTranscriptFromAudio(File localFile) throws IOException, InterruptedException {
        File wavFile = File.createTempFile("encode_wav_", ".wav");
        File wavFileNoSilence = File.createTempFile("encode_wav_no_silence_", ".wav");
        File wavFileNoHeaders = File.createTempFile("encode_wav_noheader_", ".wav");

        try {
            convertAudioTo16bit1Ch(localFile, wavFile);
            removeSilenceFromBeginning(wavFile, wavFileNoSilence);

            long silenceFileSizeDiff = wavFile.length() - wavFileNoSilence.length();
            double timeOffsetInSec = (double) silenceFileSizeDiff / BYTES_PER_SAMPLE / SAMPLES_PER_SECOND;

            WavFileUtil.fixWavHeaders(wavFileNoSilence, wavFileNoHeaders); // TODO patch sphinx to handle headers correctly

            String sphinxOutput = runSphinx(wavFileNoHeaders);

            return SphinxOutputParser.parse(sphinxOutput, timeOffsetInSec);
        } finally {
            wavFile.delete();
            wavFileNoSilence.delete();
            wavFileNoHeaders.delete();
        }
    }

    private String runSphinx(File inFile) throws IOException, InterruptedException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            processRunner.execute(
                    "pocketsphinx_continuous",
                    new String[]{
                            "-infile", inFile.getAbsolutePath(),
                            "-time", "true"
                    },
                    out,
                    inFile.getAbsolutePath() + ": "
            );
        } finally {
            out.close();
        }
        return new String(out.toByteArray());
    }

    private void removeSilenceFromBeginning(File inFile, File outFile) throws IOException, InterruptedException {
        processRunner.execute(
                "sox",
                new String[]{
                        inFile.getAbsolutePath(),
                        outFile.getAbsolutePath(),
                        "silence", "1", "0.1", "1%", // remove silence from beginning. at least 0.1s of less than 1% volume
                        "pad", "1", "0" // pad 1 second of silence to beginning
                },
                null,
                inFile.getAbsolutePath() + ": "
        );
    }

    private void convertAudioTo16bit1Ch(File inputFile, File outputFile) throws IOException, InterruptedException {
        processRunner.execute(
                "ffmpeg",
                new String[]{
                        "-y", // overwrite output files
                        "-i", inputFile.getAbsolutePath(),
                        "-acodec", "pcm_s16le",
                        "-ac", "1",
                        "-ar", Long.toString(SAMPLES_PER_SECOND),
                        outputFile.getAbsolutePath()
                },
                null,
                inputFile.getAbsolutePath() + ": "
        );
    }

    @Inject
    public void setProcessRunner(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }
}
