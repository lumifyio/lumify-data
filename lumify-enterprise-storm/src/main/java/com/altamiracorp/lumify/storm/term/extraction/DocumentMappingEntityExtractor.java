package com.altamiracorp.lumify.storm.term.extraction;

import static com.google.common.base.Preconditions.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.structuredData.mapping.DocumentMapping;
import com.altamiracorp.securegraph.Vertex;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

public class DocumentMappingEntityExtractor {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(DocumentMappingEntityExtractor.class);

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public TermExtractionResult extract(Vertex artifactVertex, User user) throws IOException, ParseException {
        checkNotNull(artifactVertex);
        checkNotNull(user);
        TermExtractionResult termExtractionResult = null;
        String artifactRowKey = (String) artifactVertex.getPropertyValue(PropertyName.ROW_KEY.toString(), 0);
        LOGGER.debug("Processing graph vertex [%s] for artifact: %s", artifactVertex.getId(), artifactRowKey);

        String mappingJsonString = (String) artifactVertex.getPropertyValue(PropertyName.MAPPING_JSON.toString(), 0);
        if (mappingJsonString != null) {
            DocumentMapping mapping = JSON_MAPPER.readValue(mappingJsonString, DocumentMapping.class);
            String text = (String) artifactVertex.getPropertyValue(PropertyName.TEXT.toString(), 0);
            termExtractionResult = mapping.mapDocument(new StringReader(text), getClass().getName());
        }
        return termExtractionResult;
    }

}
