package com.altamiracorp.reddawn.ucd.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class ArtifactTest {
  @Test
  public void createAnArtifact() {
    Artifact.Builder a = Artifact.newBuilder();

    ArtifactContent.Builder ac = ArtifactContent.newBuilder();
    ac.security("UNCLASSIFIED");
    ac.docExtractedText("This is some test text");
    ac.docArtifactBytes("This is some test text".getBytes());

    ArtifactGenericMetadata.Builder ag = ArtifactGenericMetadata.newBuilder();
    ag.author("testAuthor");
    ag.category("testCategory");
    ag.charset("testCharset");
    ag.documentDtg("testDocumentDtg");
    ag.documentType("testDocumentType");
    ag.externalUrl("testExternalUrl");
    ag.extractedTextHdfsPath("testExtractedTextHdfsPath");
    ag.fileExtension("testFileExtension");
    ag.fileName("testFileName");
    ag.fileSize(12L);
    ag.fileTimestamp(123L);
    ag.hdfsFilePath("testHdfsFilePath");
    ag.language("testLanguage");
    ag.loadTimestamp(1234L);
    ag.loadType("testLoadType");
    ag.mimeType("testMimeType");
    ag.source("testSource");
    ag.sourceSubType("sourceSubType");
    ag.sourceType("sourceType");
    ag.subject("testing document subject");

    ArtifactDynamicMetadata.Builder ad = ArtifactDynamicMetadata.newBuilder();
    ad.artifactSerialNum("testArtifactSerialNum");
    ad.docSourceHash("testDocSourceHash");
    ad.edhGuid("testEdhGuid");
    ad.geoLocation("testGeoLocation");
    ad.provenanceId("testProvenanceId");
    ad.sourceHashAlgorithm("testSourceHashAlgorithm");
    ad.sourceLabel("testSourceLabel");

    // TODO figure out what artifact annotations are
//    ad.addUnstructuredAnnotation(createAnnot("1"));
//    ad.addUnstructuredAnnotation(createAnnot("2"));
//    ad.addUnstructuredAnnotation(createAnnot("3"));

//    ad.addStructuredAnnotation(createStructAnnot("1"));
//    ad.addStructuredAnnotation(createStructAnnot("2"));
//    ad.addStructuredAnnotation(createStructAnnot("3"));

    Artifact artifact = a
        .artifactContent(ac.build())
        .artifactGenericMetadata(ag.build())
        .artifactDynamicMetadata(ad.build())
        .build();

    assertEquals("urn\u001Fsha256\u001Ff4544fb4c5af57bf6a823fbb623de44363cb3384928ca95360baede7b2e578c5", artifact.getKey().toString());
    assertEquals("UNCLASSIFIED", artifact.getContent().getSecurity());
    assertEquals("This is some test text", artifact.getContent().getDocExtractedText());
    assertEquals("This is some test text", new String(artifact.getContent().getDocArtifactBytes()));

    assertEquals("testAuthor", artifact.getGenericMetadata().getAuthor());
    assertEquals("testCategory", artifact.getGenericMetadata().getCategory());
    assertEquals("testCharset", artifact.getGenericMetadata().getCharset());
    assertEquals("testDocumentDtg", artifact.getGenericMetadata().getDocumentDtg());
    assertEquals("testDocumentType", artifact.getGenericMetadata().getDocumentType());
    assertEquals("testExternalUrl", artifact.getGenericMetadata().getExternalUrl());
    assertEquals("testExtractedTextHdfsPath", artifact.getGenericMetadata().getExtractedTextHdfsPath());
    assertEquals("testFileExtension", artifact.getGenericMetadata().getFileExtension());
    assertEquals("testFileName", artifact.getGenericMetadata().getFileName());
    assertEquals(12L, artifact.getGenericMetadata().getFileSize().longValue());
    assertEquals(123L, artifact.getGenericMetadata().getFileTimestamp().longValue());
    assertEquals("testHdfsFilePath", artifact.getGenericMetadata().getHdfsFilePath());
    assertEquals("testLanguage", artifact.getGenericMetadata().getLanguage());
    assertEquals(1234L, artifact.getGenericMetadata().getLoadTimestamp().longValue());
    assertEquals("testLoadType", artifact.getGenericMetadata().getLoadType());
    assertEquals("testMimeType", artifact.getGenericMetadata().getMimeType());
    assertEquals("testSource", artifact.getGenericMetadata().getSource());
    assertEquals("sourceSubType", artifact.getGenericMetadata().getSourceSubType());
    assertEquals("sourceType", artifact.getGenericMetadata().getSourceType());
    assertEquals("testing document subject", artifact.getGenericMetadata().getSubject());

    assertEquals("testArtifactSerialNum", artifact.getDynamicMetadata().getArtifactSerialNum());
    assertEquals("testDocSourceHash", artifact.getDynamicMetadata().getDocSourceHash());
    assertEquals("testEdhGuid", artifact.getDynamicMetadata().getEdhGuid());
    assertEquals("testGeoLocation", artifact.getDynamicMetadata().getGeoLocation());
    assertEquals("testProvenanceId", artifact.getDynamicMetadata().getProvenanceId());
    assertEquals("testSourceHashAlgorithm", artifact.getDynamicMetadata().getSourceHashAlgorithm());
    assertEquals("testSourceLabel", artifact.getDynamicMetadata().getSourceLabel());
  }
}
