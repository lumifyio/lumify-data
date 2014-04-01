package com.altamiracorp.lumify.ccextractor;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkResult;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import com.altamiracorp.lumify.core.model.properties.MediaLumifyProperties;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.util.ProcessRunner;
import com.altamiracorp.lumify.storm.video.SubRip;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.mutation.ExistingElementMutation;
import com.google.inject.Inject;

import java.io.File;
import java.io.InputStream;

public class CCExtractorGraphPropertyWorker extends GraphPropertyWorker {
    private static final String PROPERTY_KEY = CCExtractorGraphPropertyWorker.class.getName();
    private ProcessRunner processRunner;

    @Override
    public GraphPropertyWorkResult execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        File ccFile = File.createTempFile("ccextract", "txt");
        ccFile.delete();
        try {
            processRunner.execute(
                    "ccextractor",
                    new String[]{
                            "-o", ccFile.getAbsolutePath(),
                            "-in=mp4",
                            data.getLocalFile().getAbsolutePath()
                    },
                    null,
                    data.getLocalFile().getAbsolutePath() + ": "
            );

            VideoTranscript videoTranscript = SubRip.read(ccFile);

            ExistingElementMutation<Vertex> m = data.getVertex().prepareMutation();
            MediaLumifyProperties.VIDEO_TRANSCRIPT.addPropertyValue(m, PROPERTY_KEY, videoTranscript, data.getVertex().getVisibility());
            m.save();

            getGraph().flush();
            getWorkQueueRepository().pushGraphPropertyQueue(data.getVertex().getId(), PROPERTY_KEY, MediaLumifyProperties.VIDEO_TRANSCRIPT.getKey());
        } finally {
            ccFile.delete();
        }

        return new GraphPropertyWorkResult();
    }

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        if (!property.getName().equals(RawLumifyProperties.RAW.getKey())) {
            return false;
        }
        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.METADATA_MIME_TYPE);
        if (mimeType == null || !mimeType.startsWith("video")) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isLocalFileRequired() {
        return true;
    }

    @Inject
    public void setProcessRunner(ProcessRunner ffmpeg) {
        this.processRunner = ffmpeg;
    }
}
