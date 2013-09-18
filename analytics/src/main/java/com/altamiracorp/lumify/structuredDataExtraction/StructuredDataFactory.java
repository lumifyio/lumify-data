package com.altamiracorp.lumify.structuredDataExtraction;

import org.apache.hadoop.mapreduce.Mapper;

import java.util.HashMap;

public class StructuredDataFactory {
    private HashMap<String, StructuredDataExtractorBase> structuredDataExtractors = new HashMap<String, StructuredDataExtractorBase>();

    public StructuredDataFactory(Mapper.Context context) throws Exception {
        structuredDataExtractors.put("csv", new CsvStructuredDataExtractor());

        for (StructuredDataExtractorBase structuredDataExtractor : structuredDataExtractors.values()) {
            structuredDataExtractor.setup(context);
        }
    }

    public StructuredDataExtractorBase get(String structuredDataType) {
        return structuredDataExtractors.get(structuredDataType);
    }
}
