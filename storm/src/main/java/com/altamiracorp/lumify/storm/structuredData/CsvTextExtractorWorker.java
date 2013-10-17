package com.altamiracorp.lumify.storm.structuredData;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.structuredData.StructuredDataExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

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
        while (entry != null) {
            // Writing the csv to hdfs & getting a stream of the csv file
            if (entry.getName().startsWith(".")) {
                entry = fileStream.getNextTarEntry();
                continue;
            }
            if (FilenameUtils.getExtension(entry.getName()).equals("csv")) {
                csvFileName = "/lumify/data/tmp/" + entry.getName();
                FSDataOutputStream dataOutputStream = data.getHdfsFileSystem().create(new Path(csvFileName), false);

                byte[] btoRead = new byte[1024];
                int length = 0;

                // TODO: write csv data into raw column in accumulo
                while ((length = fileStream.read(btoRead)) != -1) {
                    dataOutputStream.write(btoRead, 0, length);
                }
                dataOutputStream.close();
            }
            entry = fileStream.getNextTarEntry();
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
        info.setTitle(FilenameUtils.getName(csvFileName));
        data.getHdfsFileSystem().delete(new Path(csvFileName), true);

        return info;
    }

    @Override
    public String getName() {
        return "csvTextExtractor";
    }

    @Override
    public void prepare(Map stormConf, User user) {
    }
}
