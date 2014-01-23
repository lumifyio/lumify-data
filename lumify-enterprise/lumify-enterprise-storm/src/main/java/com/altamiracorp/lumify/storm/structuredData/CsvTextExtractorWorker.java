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
import org.json.JSONObject;

public class CsvTextExtractorWorker
        extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>
        implements StructuredDataExtractionWorker {

    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(CsvTextExtractorWorker.class);

    private ObjectMapper mapper;

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Extracting Text from CSV [CsvTextExtractorWorker]: %s", data.getFileName());
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();

        // Extract mapping json
//        JSONObject mappingJson = readMappingJson(data);
        DocumentMapping mapping = readMappingJson(data);

        // Extract the csv text
        StringWriter writer = new StringWriter();
        mapping.ingestDocument(work, writer);
//        CsvPreference csvPreference = CsvPreference.EXCEL_PREFERENCE;
//        CsvListReader csvListReader = new CsvListReader(new InputStreamReader(work), csvPreference);
//        CsvListWriter csvListWriter = new CsvListWriter(writer, csvPreference);
//        List<String> line;
//
//        while ((line = csvListReader.read()) != null) {
//            csvListWriter.write(line);
//        }
//        csvListWriter.close();

        info.setText(writer.toString());
//        if (mappingJson.has(MappingProperties.SUBJECT)) {
//            info.setTitle(mappingJson.get(MappingProperties.SUBJECT).toString());
//        }
//        info.setMappingJson(mappingJson);
        info.setTitle(mapping.getSubject());
        info.setMappingJson(new JSONObject(mapper.writeValueAsString(mapping)));
        info.setConceptType(DisplayType.DOCUMENT.toString());
        LOGGER.debug("Finished [CsvTextExtractorWorker]: %s", data.getFileName());
        return info;
    }

    private DocumentMapping readMappingJson(AdditionalArtifactWorkData data) throws IOException {
        File tempDir = data.getArchiveTempDir();
        checkNotNull(tempDir, "Structured data must be an archive file");
        checkState(tempDir.isDirectory(), "Archive temp directory not a directory");
        for (File f : tempDir.listFiles()) {
            if (!f.getName().startsWith(".") && f.getName().endsWith(StructuredDataContentTypeSorter.MAPPING_JSON_FILE_NAME_SUFFIX)) {
                return mapper.readValue(f, DocumentMapping.class);
//                return new JSONObject(FileUtils.readFileToString(f));
            }
        }
        throw new RuntimeException("Could not find mapping.json file in directory: " + tempDir);
    }

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) {
        mapper = new ObjectMapper();
    }
}
