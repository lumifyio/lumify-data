package io.lumify.youtube;

import io.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import io.lumify.core.ingest.video.VideoTranscript;
import io.lumify.core.model.properties.MediaLumifyProperties;
import io.lumify.core.model.properties.RawLumifyProperties;
import org.securegraph.Element;
import org.securegraph.Property;
import org.securegraph.Vertex;
import org.securegraph.mutation.ExistingElementMutation;
import org.securegraph.property.StreamingPropertyValue;

import java.io.InputStream;

public class YoutubeTranscriptGraphPropertyWorker extends GraphPropertyWorker {
    private static final String PROPERTY_KEY = YoutubeTranscriptGraphPropertyWorker.class.getName();

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        StreamingPropertyValue youtubeccValue = YoutubeTranscriptFileImportSupportingFileHandler.YOUTUBE_CC.getPropertyValue(data.getElement());
        VideoTranscript videoTranscript = YoutubeccReader.read(youtubeccValue.getInputStream());

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

        StreamingPropertyValue youtubeccValue = YoutubeTranscriptFileImportSupportingFileHandler.YOUTUBE_CC.getPropertyValue(element);
        if (youtubeccValue == null) {
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
