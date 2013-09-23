package com.altamiracorp.lumify.entityExtraction;

import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;

public class BaseExtractorTest {

    protected Artifact createArtifact(String text) {
        ArtifactRowKey artifactRowKey = ArtifactRowKey.build(text.getBytes());
        Artifact artifact = new Artifact(artifactRowKey);
        artifact.getContent().setDocExtractedText(text.getBytes());
        artifact.getGenericMetadata().setSubject("testSubject");

        return artifact;
    }
}
