package com.altamiracorp.lumify.storm.structuredData;

import com.altamiracorp.lumify.FileImporter;
import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.structuredData.StructuredDataExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class CsvTextExtractorWorker
        extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>
        implements StructuredDataExtractionWorker {

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();

        // Untar the file
        TarArchiveInputStream fileStream = new TarArchiveInputStream(work);
        TarArchiveEntry entry = fileStream.getNextTarEntry();
        String csvFileName = "";
        String csvMapping = "";
        while (entry != null) {
            // Writing the csv to hdfs & getting a stream of the csv file
            if (entry.getName().startsWith(".")) {
                entry = fileStream.getNextTarEntry();
                continue;
            }
            String filepath = "/lumify/data/tmp/" + entry.getName();
            if (FilenameUtils.getExtension(entry.getName()).equals("csv")) {
                csvFileName = "/lumify/data/tmp/" + entry.getName();
                writeToHdfs(data.getHdfsFileSystem(), csvFileName, fileStream);
            } else if (FilenameUtils.getName(entry.getName()).contains(FileImporter.MAPPING_JSON_FILE_NAME_SUFFIX)) {
                csvMapping = filepath;
                writeToHdfs(data.getHdfsFileSystem(), csvMapping, fileStream);
            }
            entry = fileStream.getNextTarEntry();
        }

        // Extract mapping json
        JSONObject mappingJson = new JSONObject();
        InputStream mappingJsonStream = data.getHdfsFileSystem().open(new Path(csvMapping));
        try {
            mappingJson = new JSONObject(IOUtils.toString(mappingJsonStream));
        } finally {
            mappingJsonStream.close();
        }

        // Extract the csv text
        StringWriter writer = new StringWriter();
        CsvPreference csvPreference = CsvPreference.EXCEL_PREFERENCE;
        InputStream csvFileStream = data.getHdfsFileSystem().open(new Path(csvFileName));
        CsvListReader csvListReader = new CsvListReader(new InputStreamReader(csvFileStream), csvPreference);
        CsvListWriter csvListWriter = new CsvListWriter(writer, csvPreference);
        List<String> line;

        while ((line = csvListReader.read()) != null) {
            csvListWriter.write(line);
        }
        csvListWriter.close();

        info.setText(writer.toString());
        if (mappingJson.has("subject")) {
            info.setTitle(mappingJson.get("subject").toString());
        } else {
            info.setTitle(FilenameUtils.getName(csvFileName));
        }
        info.setMappingJson(mappingJson);
        data.getHdfsFileSystem().delete(new Path(csvFileName), true);
        data.getHdfsFileSystem().delete(new Path(csvMapping), true);

        return info;
    }

    @Override
    public String getName() {
        return "csvTextExtractor";
    }

    @Override
    public void prepare(Map stormConf, User user) {
    }

    private void writeToHdfs(FileSystem fileSystem, String filePath, TarArchiveInputStream fileStream) throws IOException {
        FSDataOutputStream dataOutputStream = fileSystem.create(new Path(filePath), false);

        byte[] btoRead = new byte[1024];
        int length = 0;

        // TODO: write csv data into raw column in accumulo
        while ((length = fileStream.read(btoRead)) != -1) {
            dataOutputStream.write(btoRead, 0, length);
        }
        dataOutputStream.close();
    }
}
