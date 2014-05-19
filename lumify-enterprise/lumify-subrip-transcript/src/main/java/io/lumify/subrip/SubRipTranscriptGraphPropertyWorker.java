package io.lumify.subrip;

import io.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import io.lumify.core.ingest.video.VideoTranscript;
import io.lumify.core.model.properties.MediaLumifyProperties;
import io.lumify.core.model.properties.RawLumifyProperties;
import io.lumify.storm.video.SubRip;
import org.securegraph.Element;
import org.securegraph.Property;
import org.securegraph.Vertex;
import org.securegraph.mutation.ExistingElementMutation;
import org.securegraph.property.StreamingPropertyValue;

import java.io.InputStream;

public class SubRipTranscriptGraphPropertyWorker extends GraphPropertyWorker {
    private static final String PROPERTY_KEY = SubRipTranscriptGraphPropertyWorker.class.getName();

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        StreamingPropertyValue youtubeccValue = SubRipTranscriptFileImportSupportingFileHandler.SUBRIP_CC.getPropertyValue(data.getElement());
        VideoTranscript videoTranscript = SubRip.read(youtubeccValue.getInputStream());

        ExistingElementMutation<Vertex> m = data.getElement().prepareMutation();
        MediaLumifyProperties.VIDEO_TRANSCRIPT.addPropertyValue(m, PROPERTY_KEY, videoTranscript, data.getPropertyMetadata(), data.getVisibility());
        m.save();

        getGraph().flush();
        getWorkQueueRepository().pushGraphPropertyQueue(data.getElement(), PROPERTY_KEY, MediaLumifyProperties.VIDEO_TRANSCRIPT.getKey());
    }


    @Override
    public boolean isHandled(Element element, Property property) {
        if (property == null) {
            return false;
        }

        StreamingPropertyValue subripValue = SubRipTranscriptFileImportSupportingFileHandler.SUBRIP_CC.getPropertyValue(element);
        if (subripValue == null) {
            return false;
        }

        if (!property.getName().equals(RawLumifyProperties.RAW.getKey())) {
            return false;
        }
        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.MIME_TYPE.getKey());
        if (mimeType == null || !mimeType.startsWith("video")) {
            return false;
        }

        return true;
    }
}
