package com.altamiracorp.lumify.mapping;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkResult;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.model.properties.LumifyProperties;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.mutation.ExistingElementMutation;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DocumentMappingGraphPropertyWorker extends GraphPropertyWorker {
    private static final String MULTIVALUE_KEY = DocumentMappingGraphPropertyWorker.class.getName();
    private ObjectMapper jsonMapper;

    @Override
    public GraphPropertyWorkResult execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        StreamingPropertyValue mappingJson = MappingFileImportSupportingFileHandler.MAPPING_JSON.getPropertyValue(data.getVertex());
        String mappingJsonString = IOUtils.toString(mappingJson.getInputStream());
        DocumentMapping mapping = jsonMapper.readValue(mappingJsonString, DocumentMapping.class);

        if (data.getProperty().getName().equals(RawLumifyProperties.RAW.getKey())) {
            executeTextExtraction(in, data, mapping);
        } else {
            executeTermExtraction(in, data, mapping);
        }
        return new GraphPropertyWorkResult();
    }

    private void executeTextExtraction(InputStream in, GraphPropertyWorkData data, DocumentMapping mapping) throws IOException {
        StringWriter writer = new StringWriter();
        mapping.ingestDocument(in, writer);

        ExistingElementMutation<Vertex> m = data.getVertex().prepareMutation();
        StreamingPropertyValue textValue = new StreamingPropertyValue(new ByteArrayInputStream(writer.toString().getBytes()), String.class);
        Map<String, Object> textMetadata = new HashMap<String, Object>();
        textMetadata.put(RawLumifyProperties.METADATA_MIME_TYPE, "text/plain");
        RawLumifyProperties.TEXT.addPropertyValue(m, MULTIVALUE_KEY, textValue, textMetadata, data.getVertex().getVisibility());
        LumifyProperties.TITLE.addPropertyValue(m, MULTIVALUE_KEY, mapping.getSubject(), data.getVertex().getVisibility());
        m.save();

        getGraph().flush();

        getWorkQueueRepository().pushGraphPropertyQueue(data.getVertex().getId(), MULTIVALUE_KEY, LumifyProperties.TITLE.getKey());
        getWorkQueueRepository().pushGraphPropertyQueue(data.getVertex().getId(), MULTIVALUE_KEY, RawLumifyProperties.TEXT.getKey());
    }

    private void executeTermExtraction(InputStream in, GraphPropertyWorkData data, DocumentMapping mapping) throws IOException {
        TermExtractionResult termExtractionResult = mapping.mapDocument(new InputStreamReader(in), getClass().getName());
        saveTermExtractionResult(data.getVertex(), termExtractionResult, data.getVertex().getVisibility());
    }

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        StreamingPropertyValue mappingJson = MappingFileImportSupportingFileHandler.MAPPING_JSON.getPropertyValue(vertex);
        if (mappingJson == null) {
            return false;
        }

        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.METADATA_MIME_TYPE);
        return !(mimeType == null || !mimeType.startsWith("text"));
    }

    @Inject
    public void setJsonMapper(final ObjectMapper mapper) {
        this.jsonMapper = mapper;
    }
}
