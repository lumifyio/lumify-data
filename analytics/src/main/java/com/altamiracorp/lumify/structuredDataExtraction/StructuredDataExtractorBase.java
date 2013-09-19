package com.altamiracorp.lumify.structuredDataExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.textExtraction.ArtifactExtractedInfo;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONObject;

public abstract class StructuredDataExtractorBase {
    public void setup(Mapper.Context context) throws Exception {

    }

    public abstract ExtractedData extract(Artifact artifact, String text, JSONObject mappingJson, User user) throws Exception;

    public abstract ArtifactExtractedInfo extractText(Artifact artifact, User user) throws Exception;
}
