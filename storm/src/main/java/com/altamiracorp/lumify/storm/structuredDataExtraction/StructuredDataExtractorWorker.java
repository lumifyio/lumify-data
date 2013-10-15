package com.altamiracorp.lumify.storm.structuredDataExtraction;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.structuredData.StructuredDataExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.HdfsLimitOutputStream;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.textExtraction.TikaTextExtractor;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class StructuredDataExtractorWorker
        extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>
            implements StructuredDataExtractionWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(StructuredDataExtractorWorker.class.getName());
    private TikaTextExtractor tikaTextExtractor;

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        //todo add mapping reference and related work
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();

        StringWriter writer = new StringWriter();
        CsvPreference csvPrefs = CsvPreference.EXCEL_PREFERENCE;
        CsvListReader csvReader = new CsvListReader(new InputStreamReader(work), csvPrefs);
        CsvListWriter csvWriter = new CsvListWriter(writer, csvPrefs);
        List<String> line;
        while ((line = csvReader.read()) != null) {
            csvWriter.write(line);
        }
        csvWriter.close();

        info.setText(writer.toString());

        return info;
    }

    @Inject
    public void setTikaTextExtractor(TikaTextExtractor tikaTextExtractor) {
        this.tikaTextExtractor = tikaTextExtractor;
    }

    @Override
    public void prepare(Map stormConf, User user) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
