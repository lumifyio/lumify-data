package com.altamiracorp.lumify.storm.audio;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.audio.AudioTextExtractionWorker;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.textExtraction.SphinxAudioTextExtractor;
import com.google.inject.Inject;

import java.io.InputStream;

public class AudioTextExtractorWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements AudioTextExtractionWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(AudioTextExtractorWorker.class);
    private SphinxAudioTextExtractor sphinxAudioTextExtractor;

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) throws Exception {

    }

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Extracting Image Text [AudioTextExtractorWorker]: %s", data.getFileName());
        ArtifactExtractedInfo info = sphinxAudioTextExtractor.extractFromAudio(work, data);
        if (info == null) {
            return null;
        }
        LOGGER.debug("Finished [AudioTextExtractorWorker]: %s", data.getFileName());
        return info;
    }

    @Inject
    public void setSphinxAudioTextExtractor(SphinxAudioTextExtractor sphinxAudioTextExtractor) {
        this.sphinxAudioTextExtractor = sphinxAudioTextExtractor;
    }
}
