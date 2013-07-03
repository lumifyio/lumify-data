package com.altamiracorp.reddawn.contentTypeExtraction;

import org.apache.hadoop.mapreduce.Mapper;

import java.io.InputStream;

public interface ContentTypeExtractor {
    void setup(Mapper.Context context);

    String extract(InputStream in) throws Exception;
}