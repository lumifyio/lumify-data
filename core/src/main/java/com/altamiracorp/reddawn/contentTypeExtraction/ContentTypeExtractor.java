package com.altamiracorp.reddawn.contentTypeExtraction;

import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONObject;

import java.io.InputStream;

public interface ContentTypeExtractor {
    void setup(Mapper.Context context);

    public String extract(InputStream in, String fileExt) throws Exception;

    public JSONObject extractImageMetadata(InputStream in) throws Exception;
}
