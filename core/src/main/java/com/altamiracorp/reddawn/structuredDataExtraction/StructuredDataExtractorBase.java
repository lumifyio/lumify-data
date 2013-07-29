package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONObject;

public abstract class StructuredDataExtractorBase {
    public void setup(Mapper.Context context) throws Exception {

    }

    public abstract ExtractedData extract(Artifact artifact, String text, JSONObject mappingJson) throws Exception;
}
