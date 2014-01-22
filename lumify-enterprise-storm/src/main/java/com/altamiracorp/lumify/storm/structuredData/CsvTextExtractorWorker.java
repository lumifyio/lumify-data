package com.altamiracorp.lumify.storm.structuredData;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.structuredData.StructuredDataExtractionWorker;
import com.altamiracorp.lumify.core.model.ontology.DisplayType;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class CsvTextExtractorWorker
        extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>
        implements StructuredDataExtractionWorker {

    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(CsvTextExtractorWorker.class);

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Extracting Text from CSV [CsvTextExtractorWorker]: %s", data.getFileName());
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();

        // Extract mapping json
        JSONObject mappingJson = readMappingJson(data);

        // Extract the csv text
        StringWriter writer = new StringWriter();
        CsvPreference csvPreference = CsvPreference.EXCEL_PREFERENCE;
        CsvListReader csvListReader = new CsvListReader(new InputStreamReader(work), csvPreference);
        CsvListWriter csvListWriter = new CsvListWriter(writer, csvPreference);
        List<String> line;

        while ((line = csvListReader.read()) != null) {
            csvListWriter.write(line);
        }
        csvListWriter.close();

        info.setText(writer.toString());
        if (mappingJson.has(MappingProperties.SUBJECT)) {
            info.setTitle(mappingJson.get(MappingProperties.SUBJECT).toString());
        }
        info.setMappingJson(mappingJson);
        info.setConceptType(DisplayType.DOCUMENT.toString());
        LOGGER.debug("Finished [CsvTextExtractorWorker]: %s", data.getFileName());
        return info;
    }

    private JSONObject readMappingJson(AdditionalArtifactWorkData data) throws IOException {
        File tempDir = data.getArchiveTempDir();
        checkNotNull(tempDir, "Structured data must be an archive file");
        checkState(tempDir.isDirectory(), "Archive temp directory not a directory");
        for (File f : tempDir.listFiles()) {
            if (!f.getName().startsWith(".") && f.getName().endsWith(StructuredDataContentTypeSorter.MAPPING_JSON_FILE_NAME_SUFFIX)) {
                return new JSONObject(FileUtils.readFileToString(f));
            }
        }
        throw new RuntimeException("Could not find mapping.json file in directory: " + tempDir);
    }

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) {
    }
}
