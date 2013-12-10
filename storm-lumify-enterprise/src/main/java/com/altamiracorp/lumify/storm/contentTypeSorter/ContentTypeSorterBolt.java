package com.altamiracorp.lumify.storm.contentTypeSorter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.contentTypeExtraction.ContentTypeExtractor;
import com.altamiracorp.lumify.core.ingest.ContentTypeSorter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static com.google.common.base.Preconditions.checkNotNull;

public class ContentTypeSorterBolt extends BaseLumifyBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentTypeSorterBolt.class);

    private static final Joiner FILEPATH_JOINER = Joiner.on('/');
    private static final SimpleDateFormat fileNameSuffix = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ssZ");

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
            LOGGER.info(String.format("Adding content type sorter: %s", sorter.getClass().getName()));
            contentTypeSorters.add(sorter);
        }
    }

    @Override
    public void safeExecute(Tuple input) throws Exception {
        String fileName = input.getStringByField(FieldNames.FILE_NAME);
        checkNotNull(fileName, "this bolt requires a field with name " + FieldNames.FILE_NAME);

        LOGGER.debug("Processing tuple value: " + fileName);
        InputStream in = openFile(fileName);
        try {
            String queueName = calculateQueueName(fileName, in);

            moveFile(fileName, FILEPATH_JOINER.join(dataDir, queueName, FilenameUtils.getName(fileName) + "__" + fileNameSuffix.format(new Date())));

            LOGGER.debug("Content sorted to: " + queueName);
        } finally {
            if (in != null) {
                in.close();
            }
            LOGGER.debug("[ContentTypeSorterBolt]: finished with " + fileName);
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
        try {
            ArchiveInputStream is = new ArchiveStreamFactory().createArchiveInputStream(hdfsInputStream);
            try {
                ArchiveEntry entry;
                while ((entry = is.getNextEntry()) != null) {
                    for (ContentTypeSorter contentTypeSorter : contentTypeSorters) {
                        String queueName = contentTypeSorter.getQueueNameFromArchiveEntry(entry, is);
                        if (queueName != null) {
                            return queueName;
                        }
                    }
                }
            } finally {
                is.close();
            }
        } finally {
            hdfsInputStream.close();
        }
        return null;
    }

    @Inject
    public void setContentTypeExtractor(ContentTypeExtractor contentTypeExtractor) {
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
}
