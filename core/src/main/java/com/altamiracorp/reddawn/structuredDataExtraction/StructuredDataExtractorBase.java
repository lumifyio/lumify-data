package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.textExtraction.ArtifactExtractedInfo;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONObject;

import java.io.IOException;

public abstract class StructuredDataExtractorBase {
    public void setup(Mapper.Context context) throws Exception {

    }

    public abstract ExtractedData extract(RedDawnSession session, Artifact artifact, String text, JSONObject mappingJson) throws Exception;

    public abstract ArtifactExtractedInfo extractText(Session session, Artifact artifact) throws Exception;
}
