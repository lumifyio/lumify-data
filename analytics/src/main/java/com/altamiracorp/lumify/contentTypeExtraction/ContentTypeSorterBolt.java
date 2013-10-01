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
        SimpleType contentType = toSimpleType(this.contentTypeExtractor.extract(in, FilenameUtils.getExtension(fileName)));

        pushOnQueue(contentType.toString().toLowerCase(), fileName);

        getCollector().ack(input);
    }

    @Inject
    public void setContentTypeExtractor(ContentTypeExtractor contentTypeExtractor) {
        this.contentTypeExtractor = contentTypeExtractor;
    }

    private SimpleType toSimpleType(String mimeType) {
        if (mimeType == null) {
            return SimpleType.TEXT;
        }
        mimeType = mimeType.toLowerCase();
        if (mimeType.contains("video")
                || mimeType.contains("mp4"))
            return SimpleType.VIDEO;
        else if (mimeType.contains("image"))
            return SimpleType.IMAGE;
        else
            return SimpleType.TEXT;
    }

    public static enum SimpleType {
        IMAGE, TEXT, VIDEO
    }
}
