package com.altamiracorp.lumify.storm.contentTypeSorter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.contentType.ContentTypeExtractor;
import com.altamiracorp.lumify.core.contentType.ContentTypeSorter;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.BaseFileSystemSpout;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.altamiracorp.lumify.storm.FieldNames;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.Path;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static com.google.common.base.Preconditions.checkNotNull;

public class ContentTypeSorterBolt extends BaseLumifyBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(ContentTypeSorterBolt.class);

    private static final Joiner FILEPATH_JOINER = Joiner.on('/');
    private static final String LUMIFY_QUEUE_FILENAME = ".lumify-queue";
    private static final Charset LUMIFY_QUEUE_CHARSET = Charset.forName("UTF-8");

    private ContentTypeExtractor contentTypeExtractor;
    private String dataDir;
    private List<ContentTypeSorter> contentTypeSorters;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);

        dataDir = (String) stormConf.get(BaseFileSystemSpout.DATADIR_CONFIG_NAME);
        checkNotNull(dataDir, BaseFileSystemSpout.DATADIR_CONFIG_NAME + " is a required configuration parameter");

        contentTypeSorters = Lists.newArrayList();
        for (ContentTypeSorter sorter : ServiceLoader.load(ContentTypeSorter.class)) {
            LOGGER.info("Adding content type sorter: %s", sorter.getClass().getName());
            contentTypeSorters.add(sorter);
        }
    }

    @Override
    public void safeExecute(Tuple input) throws Exception {
        String fileName = input.getStringByField(FieldNames.FILE_NAME);
        checkNotNull(fileName, "this bolt requires a field with name " + FieldNames.FILE_NAME);

        LOGGER.debug("Processing tuple value: %s", fileName);
        InputStream in = openFile(fileName);
        try {
            String queueName = calculateQueueName(fileName, in);
            // TODO: Secure this somewhat to prevent injection attacks with invalid queue names.
            // Maybe restrict queue names to [-_a-zA-Z0-9]*
            mkdir(FILEPATH_JOINER.join(dataDir, queueName));

            moveFile(fileName, FILEPATH_JOINER.join(dataDir, queueName, getFileNameWithDateSuffix(fileName)));

            LOGGER.debug("Content sorted to: %s", queueName);
        } finally {
            if (in != null) {
                in.close();
            }
            LOGGER.debug("[ContentTypeSorterBolt]: finished with %s", fileName);
        }
    }

    private String calculateQueueName(String fileName, InputStream in) throws Exception {
        String mimeType = contentTypeExtractor.extract(in, FilenameUtils.getExtension(fileName));
        String queueName = calculateQueueNameFromMimeType(mimeType);
        if (queueName != null) {
            return queueName;
        }

        if (isArchive(fileName)) {
            queueName = calculateQueueNameFromArchiveContents(fileName);
            if (queueName != null) {
                return queueName;
            }
        }

        return "document";
    }

    private String calculateQueueNameFromArchiveContents(String fileName) throws IOException, ArchiveException {
        InputStream hdfsInputStream = new BufferedInputStream(getHdfsFileSystem().open(new Path(fileName)));
        String queueName = null;
        try {
            ArchiveInputStream is = new ArchiveStreamFactory().createArchiveInputStream(hdfsInputStream);
            try {
                ArchiveEntry entry;
                boolean foundQueueFile = false;
                while ((entry = is.getNextEntry()) != null && !foundQueueFile) {
                    // if the queue file is found; read its contents and set them as the
                    // queue name, skipping all further entries; otherwise, if a queueName
                    // has not already been identified, process the file through the configured
                    // ContentTypeSorters.
                    //
                    // All archive entries will be processed until/unless the Lumify Queue File
                    // is discovered.  If it is found, its contents will override any discovered
                    // queue from the ContentTypeSorters.
                    if (LUMIFY_QUEUE_FILENAME.equalsIgnoreCase(entry.getName())) {
                        foundQueueFile = true;
                        String foundQueue = readQueueFromQueueFile(entry, is);
                        if (foundQueue != null) {
                            queueName = foundQueue;
                        }
                    } else if (queueName == null) {
                        for (ContentTypeSorter contentTypeSorter : contentTypeSorters) {
                            queueName = contentTypeSorter.getQueueNameFromArchiveEntry(entry, is);
                            if (queueName != null) {
                                break;
                            }
                        }
                    }
                }
            } finally {
                is.close();
            }
        } finally {
            hdfsInputStream.close();
        }
        return queueName;
    }

    @Inject
    public void setContentTypeExtractor(@Nullable ContentTypeExtractor contentTypeExtractor) {
        this.contentTypeExtractor = contentTypeExtractor;
    }

    private String calculateQueueNameFromMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }

        for (ContentTypeSorter contentTypeSorter : contentTypeSorters) {
            String queueName = contentTypeSorter.getQueueNameFromMimeType(mimeType);
            if (queueName != null) {
                return queueName;
            }
        }

        return null;
    }

    /**
     * Reads the first line of the UTF-8 formatted Lumify Queue File and, if
     * it is not empty, returns that line as the queue name; otherwise it
     * returns null.
     *
     * @param queueFileEntry the archive entry for the queue file
     * @param archiveIn      the archive input stream
     * @return the identified queue name or null if it could not be resolved
     * @throws IOException if an error occurs while reading the queue file
     */
    private String readQueueFromQueueFile(final ArchiveEntry queueFileEntry, final InputStream archiveIn) throws IOException {
        String queue = null;
        long entrySize = queueFileEntry.getSize();
        if (entrySize > Integer.MAX_VALUE) {
            LOGGER.error("Lumify Queue File (%s) is too large [%d bytes] to process and will be ignored.", LUMIFY_QUEUE_FILENAME, entrySize);
        } else {
            byte[] contents = new byte[(int) entrySize];
            int readCount = archiveIn.read(contents);
            if (readCount <= 0) {
                LOGGER.warn("0 bytes read from Lumify Queue File. Skipping.");
            } else {
                BufferedReader queueReader = new BufferedReader(new StringReader(new String(contents, LUMIFY_QUEUE_CHARSET)));
                queue = queueReader.readLine().trim();
                if (queue.isEmpty()) {
                    LOGGER.warn("First line of Lumify Queue File is empty. Skipping.");
                    queue = null;
                } else {
                    return queue;
                }
            }
        }
        return queue;
    }
}
