package com.altamiracorp.lumify.storm.contentTypeSorter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.contentTypeExtraction.ContentTypeExtractor;
import com.altamiracorp.lumify.storm.BaseFileSystemSpout;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.altamiracorp.lumify.storm.FieldNames;
import com.altamiracorp.lumify.storm.StormRunner;
import com.google.inject.Inject;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ContentTypeSorterBolt extends BaseLumifyBolt {
    private ContentTypeExtractor contentTypeExtractor;
    private boolean local;
    private String dataDir;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        local = (Boolean) stormConf.get(StormRunner.LOCAL_CONFIG_KEY);

        this.dataDir = (String) stormConf.get(BaseFileSystemSpout.DATADIR_CONFIG_NAME);
        checkNotNull(this.dataDir, BaseFileSystemSpout.DATADIR_CONFIG_NAME + " is a required configuration parameter");
    }

    @Override
    public void safeExecute(Tuple input) throws Exception {
        String fileName = input.getStringByField(FieldNames.FILE_NAME);
        checkNotNull(fileName, "this bolt requires a field with name " + FieldNames.FILE_NAME);
        InputStream in = openFile(fileName);
        try {
            String mimeType = this.contentTypeExtractor.extract(in, FilenameUtils.getExtension(fileName));
            String queueName = calculateQueueNameFromMimeType(mimeType);
            if (local) {
                JSONObject json = new JSONObject();
                json.put("fileName", fileName);
                json.put("mimeType", mimeType);
                json.put("sourceBolt", getClass().getName());
                pushOnQueue(queueName, json);
            } else {
                moveFile(fileName, this.dataDir + "/" + queueName + "/" + FilenameUtils.getName(fileName));
            }

            getCollector().ack(input);
        } finally {
            in.close();
        }
    }

    @Inject
    public void setContentTypeExtractor(ContentTypeExtractor contentTypeExtractor) {
        this.contentTypeExtractor = contentTypeExtractor;
    }

    private String calculateQueueNameFromMimeType(String mimeType) {
        if (mimeType == null) {
            return "document";
        }
        mimeType = mimeType.toLowerCase();
        if (mimeType.contains("video")
                || mimeType.contains("mp4"))
            return "video";
        else if (mimeType.contains("image"))
            return "image";
        else
            return "document";
    }
}
