package com.altamiracorp.lumify.tikaMimeType;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.mutation.ExistingElementMutation;

import java.io.InputStream;

public class MimeTypeGraphPropertyWorker extends GraphPropertyWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(MimeTypeGraphPropertyWorker.class);
    private TikaMimeTypeMapper mimeTypeMapper;

    @Override
    public void prepare(GraphPropertyWorkerPrepareData workerPrepareData) throws Exception {
        super.prepare(workerPrepareData);

        mimeTypeMapper = new TikaMimeTypeMapper();
    }

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        String fileName = RawLumifyProperties.FILE_NAME.getPropertyValue(data.getVertex());
        String mimeType = mimeTypeMapper.guessMimeType(in, fileName);
        if (mimeType == null) {
            return;
        }

        ExistingElementMutation<Vertex> m = data.getVertex().prepareMutation();
        RawLumifyProperties.MIME_TYPE.setProperty(m, mimeType, data.getVertex().getVisibility());
        m.alterPropertyMetadata(data.getProperty(), RawLumifyProperties.METADATA_MIME_TYPE, mimeType);
        m.save();

        getGraph().flush();
        getWorkQueueRepository().pushGraphPropertyQueue(data.getVertex().getId(), data.getProperty().getKey(), data.getProperty().getName());
    }

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        if (!property.getName().equals(RawLumifyProperties.RAW.getKey())) {
            return false;
        }
        if (RawLumifyProperties.MIME_TYPE.getPropertyValue(vertex) != null) {
            return false;
        }

        String fileName = RawLumifyProperties.FILE_NAME.getPropertyValue(vertex);
        if (fileName == null) {
            return false;
        }

        return true;
    }
}
