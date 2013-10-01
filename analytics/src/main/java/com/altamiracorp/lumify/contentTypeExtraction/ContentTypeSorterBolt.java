package com.altamiracorp.lumify.contentTypeExtraction;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.altamiracorp.lumify.storm.FieldNames;
import com.google.inject.Inject;
import org.apache.commons.io.FilenameUtils;

import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public class ContentTypeSorterBolt extends BaseLumifyBolt {
    private ContentTypeExtractor contentTypeExtractor;

    @Override
    public void safeExecute(Tuple input) throws Exception {
        String fileName = input.getStringByField(FieldNames.FILE_NAME);
        checkNotNull(fileName, "this bolt requires a field with name " + FieldNames.FILE_NAME);
        InputStream in = openFile(fileName);
        try {
            String queueName = calculateQueueNameFromMimeType(this.contentTypeExtractor.extract(in, FilenameUtils.getExtension(fileName)));
            pushOnQueue(queueName, fileName);

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
