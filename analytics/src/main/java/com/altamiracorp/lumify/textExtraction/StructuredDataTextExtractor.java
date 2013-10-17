package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.structuredDataExtraction.StructuredDataFactory;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.google.inject.Injector;
import org.apache.hadoop.mapreduce.Mapper;

public class StructuredDataTextExtractor implements TextExtractor {
    public static final String NAME = "structuredData";
    private StructuredDataFactory structedDataFactory;

    @Override
    public void setup(Mapper.Context context, Injector injector) throws Exception {
        structedDataFactory = new StructuredDataFactory(context, injector);
    }

    @Override
    public ArtifactExtractedInfo extract(Artifact artifact, User user) throws Exception {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        JSONObject mappingJson = artifact.getGenericMetadata().getMappingJson();
//        if (mappingJson == null) {
//            return null;
//        }
//        String type = mappingJson.getString("type");
//        StructuredDataExtractorBase extractor = structedDataFactory.get(type);
//        return extractor.extractText(artifact, user);
    }

    @Override
    public VideoFrameExtractedInfo extract(VideoFrame videoFrame, User user) throws Exception {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
