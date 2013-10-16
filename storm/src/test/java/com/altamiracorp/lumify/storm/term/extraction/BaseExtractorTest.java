package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.ucd.artifact.Artifact;

public class BaseExtractorTest {

    protected Artifact createArtifact(String text) {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        ArtifactRowKey artifactRowKey = ArtifactRowKey.build(text.getBytes());
//        Artifact artifact = new Artifact(artifactRowKey);
//        artifact.getContent().setDocExtractedText(text.getBytes());
//        artifact.getGenericMetadata().setSubject("testSubject");
//
//        return artifact;
    }
}
