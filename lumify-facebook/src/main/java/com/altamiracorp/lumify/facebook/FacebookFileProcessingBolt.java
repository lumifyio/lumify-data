package com.altamiracorp.lumify.facebook;


import backtype.storm.task.OutputCollector;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.BaseFileProcessingBolt;
import static com.altamiracorp.lumify.facebook.FacebookConstants.*;
import com.altamiracorp.lumify.storm.file.FileMetadata;
import com.altamiracorp.securegraph.Vertex;
import com.google.inject.Inject;
import org.apache.hadoop.fs.FileSystem;
import org.codehaus.plexus.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class FacebookFileProcessingBolt extends BaseFileProcessingBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FacebookFileProcessingBolt.class);
    private static final String PROCESS = FacebookFileProcessingBolt.class.getName();
    private static FileSystem fileSystem;
    private static Vertex savedArtifact;
    private static final Pattern JSON_OBJECT = Pattern.compile("^\\s*\\{.*\\}\\s*$");
    private static OutputCollector collector;

    @Override
    public void safeExecute(Tuple input) throws Exception {
        FileMetadata fileMetadata = getFileMetadata(input);
        LOGGER.info("Processing file: %s (mimeType: %s)",
                fileMetadata.getFileName(), fileMetadata.getMimeType());
        setHdfsFileSystem();
        setCollector();
        processFile(input, fileMetadata.getFileName());
    }

    protected void processFile(final Tuple rootTuple, final String filename) throws Exception {
        LOGGER.info("Processing file: %s", filename);
        FileMetadata fileMd = new FileMetadata()
                .setFileName(filename)
                .setMimeType(getMimeType(filename));
        String filenameNoDate = fileMd.getFileNameWithoutDateSuffix().toLowerCase();
        if (isArchive(filenameNoDate)) {
            processArchive(rootTuple, fileMd);
        } else {
            InputStream is = openFile(filename);
            // if the file is a gzip compressed file, uncompress before reading
            if (filenameNoDate.endsWith(GZIP_EXTENSION)) {
                is = new GZIPInputStream(is);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            try {
                String facebookJson = reader.readLine();
                // only process this file if the first line appears to be a JSON string
                if (facebookJson != null && JSON_OBJECT.matcher(facebookJson).matches()) {
                    while (facebookJson != null) {
                        collector.emit(rootTuple, new Values(facebookJson.trim()));
                        facebookJson = reader.readLine();
                    }
                }
            } finally {
                reader.close();
            }
        }
    }

    protected void processArchive(final Tuple rootTuple, final FileMetadata archiveMd) throws Exception {
        File archiveDir = null;
        try {
            archiveDir = extractArchive(archiveMd);
            LOGGER.info("Extracted archive [%s] to temporary directory: %s",
                    archiveMd.getFileName(), archiveDir.getAbsolutePath());
            processDirectory(rootTuple, archiveDir);
        } finally {
            if (archiveDir != null) {
                FileUtils.deleteDirectory(archiveDir);
            }
        }
    }

    protected void processDirectory(final Tuple rootTuple, final File directory) throws Exception {
        LOGGER.info("Processing files in directory: %s", directory.getAbsolutePath());
        for (File entry : directory.listFiles()) {
            if (entry.isDirectory()) {
                processDirectory(rootTuple, entry);
            } else {
                processFile(rootTuple, entry.getAbsolutePath());
            }
        }
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public User getUser() {
        return super.getUser();
    }

    private void setHdfsFileSystem() {
        this.fileSystem = getHdfsFileSystem();
    }

    public void setHdfsFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    private void setSavedArtifact(ArtifactExtractedInfo artifactExtractedInfo) {
        // TODO refactor for secure graph
//        this.savedArtifact = saveArtifact(artifactExtractedInfo);
    }

    private void setCollector() {
        this.collector = getCollector();
    }

    public void setCollector(OutputCollector outputCollector) {
        this.collector = outputCollector;
    }
}