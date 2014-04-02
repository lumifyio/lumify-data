package com.altamiracorp.lumify.subrip;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import com.altamiracorp.lumify.core.model.properties.MediaLumifyProperties;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.storm.video.SubRip;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.mutation.ExistingElementMutation;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;

import java.io.InputStream;

public class SubRipTranscriptGraphPropertyWorker extends GraphPropertyWorker {
    private static final String PROPERTY_KEY = SubRipTranscriptGraphPropertyWorker.class.getName();

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        StreamingPropertyValue youtubeccValue = SubRipTranscriptFileImportSupportingFileHandler.SUBRIP_CC.getPropertyValue(data.getVertex());
        VideoTranscript videoTranscript = SubRip.read(youtubeccValue.getInputStream());

        ExistingElementMutation<Vertex> m = data.getVertex().prepareMutation();
        MediaLumifyProperties.VIDEO_TRANSCRIPT.addPropertyValue(m, PROPERTY_KEY, videoTranscript, data.getVertex().getVisibility());
        m.save();

        getGraph().flush();
        getWorkQueueRepository().pushGraphPropertyQueue(data.getVertex().getId(), PROPERTY_KEY, MediaLumifyProperties.VIDEO_TRANSCRIPT.getKey());
    }


    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        StreamingPropertyValue subripValue = SubRipTranscriptFileImportSupportingFileHandler.SUBRIP_CC.getPropertyValue(vertex);
        if (subripValue == null) {
            return false;
        }

        if (!property.getName().equals(RawLumifyProperties.RAW.getKey())) {
            return false;
        }
        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.METADATA_MIME_TYPE);
        if (mimeType == null || !mimeType.startsWith("video")) {
            return false;
        }

        return true;
    }
}
