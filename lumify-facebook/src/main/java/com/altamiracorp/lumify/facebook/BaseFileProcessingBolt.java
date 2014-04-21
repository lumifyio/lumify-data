package com.altamiracorp.lumify.facebook;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import com.google.common.io.Files;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.*;

import static com.altamiracorp.lumify.core.model.properties.EntityLumifyProperties.SOURCE;
import static com.altamiracorp.lumify.core.model.properties.LumifyProperties.TITLE;
import static com.altamiracorp.lumify.core.model.properties.RawLumifyProperties.*;

/**
 * Base class for bolts that process files from HDFS.
 */
public abstract class BaseFileProcessingBolt extends BaseLumifyBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(BaseFileProcessingBolt.class);

    protected FileMetadata getFileMetadata(Tuple input) throws Exception {
        String fileName = input.getString(0);
        if (fileName == null || fileName.length() == 0) {
            throw new RuntimeException("Invalid item on the queue.");
        }
        String mimeType = null;
        InputStream raw = null;
        String source = null;
        String title = null;

        if (fileName.startsWith("{")) {
            JSONObject json = getJsonFromTuple(input);
            fileName = json.optString("fileName");
            mimeType = json.optString("mimeType");
            source = json.optString("source");
            title = json.optString("title");
            String rawString = json.optString("raw");

            String vertexId = json.optString("graphVertexId");
            if (vertexId != null && vertexId.length() > 0) {
                Vertex artifactVertex = graph.getVertex(vertexId, getAuthorizations());
                if (artifactVertex == null) {
                    throw new RuntimeException("Could not find vertex with id: " + vertexId);
                }
                fileName = FILE_NAME.getPropertyValue(artifactVertex);
                mimeType = MIME_TYPE.getPropertyValue(artifactVertex);
                source = SOURCE.getPropertyValue(artifactVertex);
                title = TITLE.getPropertyValue(artifactVertex);

                StreamingPropertyValue rawPropertyValue = RAW.getPropertyValue(artifactVertex);
                raw = rawPropertyValue.getInputStream();
            } else if (rawString != null) {
                raw = new ByteArrayInputStream(rawString.getBytes());
            }
        }

        FileMetadata fileMetadata = new FileMetadata()
                .setFileName(fileName)
                .setMimeType(mimeType)
                .setRaw(raw)
                .setSource(source);
        if (title != null) {
            fileMetadata.setTitle(title);
        } else {
            fileMetadata.setTitle(fileMetadata.getFileNameWithoutDateSuffix());
        }
        return fileMetadata;
    }

    /**
     * Extract an archive file to a local temporary directory.
     *
     * @param fileMetadata the file metadata
     * @return the temporary directory containing the archive contents
     */
    protected File extractArchive(final FileMetadata fileMetadata) throws Exception {
        File tempDir = Files.createTempDir();
        LOGGER.debug("Extracting %s to %s", fileMetadata.getFileName(), tempDir);
        InputStream in = openFile(fileMetadata.getFileName());
        try {
            ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(in));
            try {
                ArchiveEntry entry;
                while ((entry = input.getNextEntry()) != null) {
                    File outputFile = new File(tempDir, entry.getName());
                    OutputStream out = new FileOutputStream(outputFile);
                    try {
                        long numberOfBytesExtracted = IOUtils.copyLarge(input, out);
                        LOGGER.debug("Extracted (%d bytes) to %s", numberOfBytesExtracted, outputFile.getAbsolutePath());
                    } finally {
                        out.close();
                    }
                }
            } finally {
                input.close();
            }
        } finally {
            in.close();
        }
        return tempDir;
    }
}
