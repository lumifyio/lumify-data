package io.lumify.ccextractor;

import com.google.inject.Inject;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import io.lumify.core.ingest.video.VideoTranscript;
import io.lumify.core.model.audit.AuditAction;
import io.lumify.core.model.properties.MediaLumifyProperties;
import io.lumify.core.model.properties.RawLumifyProperties;
import io.lumify.core.util.ProcessRunner;
import io.lumify.storm.video.SubRip;
import org.securegraph.Element;
import org.securegraph.Property;
import org.securegraph.Vertex;
import org.securegraph.mutation.ExistingElementMutation;

import java.io.File;
import java.io.InputStream;

public class CCExtractorGraphPropertyWorker extends GraphPropertyWorker {
    private static final String PROPERTY_KEY = CCExtractorGraphPropertyWorker.class.getName();
    private ProcessRunner processRunner;

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
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

            ExistingElementMutation<Vertex> m = data.getElement().prepareMutation();
            MediaLumifyProperties.VIDEO_TRANSCRIPT.addPropertyValue(m, PROPERTY_KEY, videoTranscript, data.getPropertyMetadata(), data.getVisibility());
            Vertex v = m.save(getAuthorizations());
            getAuditRepository().auditVertexElementMutation(AuditAction.UPDATE, m, v, PROPERTY_KEY, getUser(), data.getVisibility());
            getAuditRepository().auditAnalyzedBy(AuditAction.ANALYZED_BY, v, getClass().getSimpleName(), getUser(), data.getVisibility());

            getGraph().flush();
            getWorkQueueRepository().pushGraphPropertyQueue(data.getElement(), PROPERTY_KEY, MediaLumifyProperties.VIDEO_TRANSCRIPT.getKey());
        } finally {
            ccFile.delete();
        }
    }

    @Override
    public boolean isHandled(Element element, Property property) {
        if (property == null) {
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

    @Override
    public boolean isLocalFileRequired() {
        return true;
    }

    @Inject
    public void setProcessRunner(ProcessRunner ffmpeg) {
        this.processRunner = ffmpeg;
    }
}
