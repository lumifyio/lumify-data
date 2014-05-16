package io.lumify.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import io.lumify.core.ingest.term.extraction.TermExtractionResult;
import io.lumify.core.model.properties.LumifyProperties;
import io.lumify.core.model.properties.RawLumifyProperties;
import org.apache.commons.io.IOUtils;
import org.securegraph.Element;
import org.securegraph.Property;
import org.securegraph.Vertex;
import org.securegraph.mutation.ExistingElementMutation;
import org.securegraph.property.StreamingPropertyValue;

import java.io.*;
import java.util.Map;

public class DocumentMappingGraphPropertyWorker extends GraphPropertyWorker {
    private static final String MULTIVALUE_KEY = DocumentMappingGraphPropertyWorker.class.getName();
    private ObjectMapper jsonMapper;

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        StreamingPropertyValue mappingJson = RawLumifyProperties.MAPPING_JSON.getPropertyValue(data.getElement());
        String mappingJsonString = IOUtils.toString(mappingJson.getInputStream());
        DocumentMapping mapping = jsonMapper.readValue(mappingJsonString, DocumentMapping.class);

        if (data.getProperty().getName().equals(RawLumifyProperties.RAW.getKey())) {
            executeTextExtraction(in, data, mapping);
        } else {
            executeTermExtraction(in, data, mapping);
        }
    }

    private void executeTextExtraction(InputStream in, GraphPropertyWorkData data, DocumentMapping mapping) throws IOException {
        StringWriter writer = new StringWriter();
        mapping.ingestDocument(in, writer);

        ExistingElementMutation<Vertex> m = data.getElement().prepareMutation();
        StreamingPropertyValue textValue = new StreamingPropertyValue(new ByteArrayInputStream(writer.toString().getBytes()), String.class);
        Map<String, Object> textMetadata = data.getPropertyMetadata();
        textMetadata.put(RawLumifyProperties.MIME_TYPE.getKey(), "text/plain");
        RawLumifyProperties.TEXT.addPropertyValue(m, MULTIVALUE_KEY, textValue, textMetadata, data.getVisibility());
        LumifyProperties.TITLE.addPropertyValue(m, MULTIVALUE_KEY, mapping.getSubject(), data.getPropertyMetadata(), data.getVisibility());
        m.save();

        getGraph().flush();

        getWorkQueueRepository().pushGraphPropertyQueue(data.getElement(), MULTIVALUE_KEY, LumifyProperties.TITLE.getKey());
        getWorkQueueRepository().pushGraphPropertyQueue(data.getElement(), MULTIVALUE_KEY, RawLumifyProperties.TEXT.getKey());
    }

    private void executeTermExtraction(InputStream in, GraphPropertyWorkData data, DocumentMapping mapping) throws IOException {
        TermExtractionResult termExtractionResult = mapping.mapDocument(new InputStreamReader(in), getClass().getName(), data.getProperty().getKey(), data.getVisibility());
        saveTermExtractionResult((Vertex) data.getElement(), termExtractionResult);
    }

    @Override
    public boolean isHandled(Element element, Property property) {
        if (property == null) {
            return false;
        }

        StreamingPropertyValue mappingJson = RawLumifyProperties.MAPPING_JSON.getPropertyValue(element);
        if (mappingJson == null) {
            return false;
        }

        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.MIME_TYPE.getKey());
        return !(mimeType == null || !mimeType.startsWith("text"));
    }

    @Inject
    public void setJsonMapper(final ObjectMapper mapper) {
        this.jsonMapper = mapper;
    }
}
