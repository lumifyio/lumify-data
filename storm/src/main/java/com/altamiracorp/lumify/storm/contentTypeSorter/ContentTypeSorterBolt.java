package com.altamiracorp.lumify.storm.contentTypeSorter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.contentTypeExtraction.ContentTypeExtractor;
import com.altamiracorp.lumify.core.ingest.ContentTypeSorter;
import com.altamiracorp.lumify.storm.BaseFileSystemSpout;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.altamiracorp.lumify.storm.FieldNames;
import com.google.inject.Inject;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.Path;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static com.google.common.base.Preconditions.checkNotNull;

public class ContentTypeSorterBolt extends BaseLumifyBolt {
    private ContentTypeExtractor contentTypeExtractor;
    private String dataDir;
    private List<ContentTypeSorter> contentTypeSorters;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);

        this.dataDir = (String) stormConf.get(BaseFileSystemSpout.DATADIR_CONFIG_NAME);
        checkNotNull(this.dataDir, BaseFileSystemSpout.DATADIR_CONFIG_NAME + " is a required configuration parameter");

        this.contentTypeSorters = new ArrayList<ContentTypeSorter>();
        for (ContentTypeSorter contentTypeSorter : ServiceLoader.load(ContentTypeSorter.class)) {
            this.contentTypeSorters.add(contentTypeSorter);
        }
    }

    @Override
    public void safeExecute(Tuple input) throws Exception {
        String fileName = input.getStringByField(FieldNames.FILE_NAME);
        checkNotNull(fileName, "this bolt requires a field with name " + FieldNames.FILE_NAME);
        InputStream in = openFile(fileName);
        try {
            String queueName = calculateQueueName(fileName, in);

            moveFile(fileName, this.dataDir + "/" + queueName + "/" + FilenameUtils.getName(fileName));

            getCollector().ack(input);
        } finally {
            in.close();
        }
    }

    private String calculateQueueName(String fileName, InputStream in) throws Exception {
        String mimeType = this.contentTypeExtractor.extract(in, FilenameUtils.getExtension(fileName));
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
                    for (ContentTypeSorter contentTypeSorter : this.contentTypeSorters) {
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

        for (ContentTypeSorter contentTypeSorter : this.contentTypeSorters) {
            String queueName = contentTypeSorter.getQueueNameFromMimeType(mimeType);
            if (queueName != null) {
                return queueName;
            }
        }

        return null;
    }
}
