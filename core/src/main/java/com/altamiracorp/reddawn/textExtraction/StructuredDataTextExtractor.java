package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.structuredDataExtraction.StructuredDataExtractorBase;
import com.altamiracorp.reddawn.structuredDataExtraction.StructuredDataFactory;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONObject;

public class StructuredDataTextExtractor implements TextExtractor {
    public static final String NAME = "structuredData";
    private StructuredDataFactory structedDataFactory;

    @Override
    public void setup(Mapper.Context context) throws Exception {
        structedDataFactory = new StructuredDataFactory(context);
    }

    @Override
    public ArtifactExtractedInfo extract(Session session, Artifact artifact) throws Exception {
        JSONObject mappingJson = artifact.getGenericMetadata().getMappingJson();
        if (mappingJson == null) {
            return null;
        }
        String type = mappingJson.getString("type");
        StructuredDataExtractorBase extractor = structedDataFactory.get(type);
        return extractor.extractText(session, artifact);
    }

    @Override
    public VideoFrameExtractedInfo extract(Session session, VideoFrame videoFrame) throws Exception {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
