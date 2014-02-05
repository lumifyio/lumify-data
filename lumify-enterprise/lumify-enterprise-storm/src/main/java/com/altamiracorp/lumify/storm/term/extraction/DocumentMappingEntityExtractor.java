package com.altamiracorp.lumify.storm.term.extraction;

import static com.altamiracorp.lumify.core.model.properties.RawLumifyProperties.MAPPING_JSON;
import static com.altamiracorp.lumify.core.model.properties.RawLumifyProperties.TEXT;
import static com.google.common.base.Preconditions.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
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
import java.text.ParseException;

public class DocumentMappingEntityExtractor {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(DocumentMappingEntityExtractor.class);

    private ObjectMapper jsonMapper;

    public TermExtractionResult extract(Vertex artifactVertex, User user) throws IOException, ParseException {
        checkNotNull(artifactVertex);
        checkNotNull(user);
        TermExtractionResult termExtractionResult = new TermExtractionResult();
        LOGGER.debug("Processing graph vertex [%s]", artifactVertex.getId());

        String mappingJsonString = MAPPING_JSON.getPropertyValue(artifactVertex);
        if (mappingJsonString != null) {
            DocumentMapping mapping = jsonMapper.readValue(mappingJsonString, DocumentMapping.class);
            StreamingPropertyValue textVal = TEXT.getPropertyValue(artifactVertex);
            termExtractionResult = mapping.mapDocument(new InputStreamReader(textVal.getInputStream()), getClass().getName());
        }
        return termExtractionResult;
    }

    @Inject
    public void setJsonMapper(final ObjectMapper mapper) {
        this.jsonMapper = mapper;
    }
}
