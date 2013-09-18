package com.altamiracorp.lumify.structuredDataExtraction;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.textExtraction.ArtifactExtractedInfo;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONObject;

public abstract class StructuredDataExtractorBase {
    public void setup(Mapper.Context context) throws Exception {

    }

    public abstract ExtractedData extract(AppSession session, Artifact artifact, String text, JSONObject mappingJson) throws Exception;

    public abstract ArtifactExtractedInfo extractText(ModelSession session, Artifact artifact) throws Exception;
}
