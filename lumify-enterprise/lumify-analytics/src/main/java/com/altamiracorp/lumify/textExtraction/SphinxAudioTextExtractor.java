package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import com.altamiracorp.lumify.core.util.ProcessRunner;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SphinxAudioTextExtractor {
    private ProcessRunner processRunner;

    public ArtifactExtractedInfo extractFromAudio(InputStream work, AdditionalArtifactWorkData data) throws IOException, InterruptedException {
        VideoTranscript transcript = extractTranscriptFromAudio(work, data);
        if (transcript == null) {
            return null;
        }

        ArtifactExtractedInfo extractedInfo = new ArtifactExtractedInfo();
        extractedInfo.setVideoTranscript(transcript);
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

            processRunner.execute(
                    "ffmpeg",
                    new String[]{
                            "-infile", wavFile.getAbsolutePath(),
                    },
                    null,
                    data.getFileName() + ": "
            );
        } finally {
            wavFile.delete();
        }

        return null;
    }

    @Inject
    public void setProcessRunner(ProcessRunner ffmpeg) {
        this.processRunner = ffmpeg;
    }
}
