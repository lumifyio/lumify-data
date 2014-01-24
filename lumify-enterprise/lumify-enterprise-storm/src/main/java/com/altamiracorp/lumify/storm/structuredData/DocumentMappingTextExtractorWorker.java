package com.altamiracorp.lumify.storm.structuredData;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.structuredData.StructuredDataExtractionWorker;
import com.altamiracorp.lumify.core.model.ontology.DisplayType;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.storm.structuredData.mapping.DocumentMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * This class applies a configured DocumentMapping to the input to process
 * and store the document text with the Artifact for future term extraction
 * by the defined mapping.
 */
public class DocumentMappingTextExtractorWorker
        extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>
        implements StructuredDataExtractionWorker {

    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(DocumentMappingTextExtractorWorker.class);

    private ObjectMapper jsonMapper;

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Extracting Text from Mapped Document [DocumentMappingTextExtractorWorker]: %s", data.getFileName());
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();

        // Extract mapping json
        DocumentMapping mapping = readMappingJson(data);

        // Extract the document text
        StringWriter writer = new StringWriter();
        mapping.ingestDocument(work, writer);
        info.setText(writer.toString());
        info.setTitle(mapping.getSubject());
        info.setMappingJson(jsonMapper.writeValueAsString(mapping));
        info.setConceptType(DisplayType.DOCUMENT.toString());
        LOGGER.debug("Finished [DocumentMappingTextExtractorWorker]: %s", data.getFileName());
        return info;
    }

    private DocumentMapping readMappingJson(AdditionalArtifactWorkData data) throws IOException {
        File tempDir = data.getArchiveTempDir();
        checkNotNull(tempDir, "Structured data must be an archive file");
        checkState(tempDir.isDirectory(), "Archive temp directory not a directory");
        for (File f : tempDir.listFiles()) {
            if (!f.getName().startsWith(".") && f.getName().endsWith(StructuredDataContentTypeSorter.MAPPING_JSON_FILE_NAME_SUFFIX)) {
                return jsonMapper.readValue(f, DocumentMapping.class);
            }
        }
        throw new RuntimeException("Could not find mapping.json file in directory: " + tempDir);
    }

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) {
        jsonMapper = new ObjectMapper();
    }
}
