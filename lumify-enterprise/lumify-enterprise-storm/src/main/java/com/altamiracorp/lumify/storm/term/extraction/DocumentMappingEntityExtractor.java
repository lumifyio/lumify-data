package com.altamiracorp.lumify.storm.term.extraction;

import static com.google.common.base.Preconditions.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.mapping.DocumentMapping;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;

public class DocumentMappingEntityExtractor {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(DocumentMappingEntityExtractor.class);

    private ObjectMapper jsonMapper;

    public TermExtractionResult extract(Vertex artifactVertex, User user) throws IOException, ParseException {
        checkNotNull(artifactVertex);
        checkNotNull(user);
        TermExtractionResult termExtractionResult = new TermExtractionResult();
        LOGGER.debug("Processing graph vertex [%s]", artifactVertex.getId());

        String mappingJsonString = (String) artifactVertex.getPropertyValue(PropertyName.MAPPING_JSON.toString());
        if (mappingJsonString != null) {
            DocumentMapping mapping = jsonMapper.readValue(mappingJsonString, DocumentMapping.class);
            Object textVal = artifactVertex.getPropertyValue(PropertyName.TEXT.toString());
            Reader textReader;
            if (textVal instanceof StreamingPropertyValue) {
                textReader = new InputStreamReader(((StreamingPropertyValue) textVal).getInputStream());
            } else {
                textReader = new StringReader(textVal.toString());
            }
            termExtractionResult = mapping.mapDocument(textReader, getClass().getName());
        }
        return termExtractionResult;
    }

    @Inject
    public void setJsonMapper(final ObjectMapper mapper) {
        this.jsonMapper = mapper;
    }
}
