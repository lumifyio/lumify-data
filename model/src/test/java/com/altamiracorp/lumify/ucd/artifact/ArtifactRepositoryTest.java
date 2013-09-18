package com.altamiracorp.lumify.ucd.artifact;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.MockSession;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.RowKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class ArtifactRepositoryTest {
    private MockSession session;
    private ArtifactRepository artifactRepository;

    @Before
    public void before() {
        session = new MockSession();
        session.initializeTables();
        artifactRepository = new ArtifactRepository();
    }

    @Test
    public void testFindByRowKey() {
        Row<RowKey> row = new Row<RowKey>(Artifact.TABLE_NAME, new RowKey("key1"));

        ColumnFamily contentColumnFamily = new ColumnFamily(ArtifactContent.NAME);
        contentColumnFamily
                .set(ArtifactContent.DOC_ARTIFACT_BYTES, "testDocArtifactBytes".getBytes())
                .set(ArtifactContent.DOC_EXTRACTED_TEXT, "testDocExtractedText".getBytes())
                .set(ArtifactContent.SECURITY, "testSecurity")
                .set(ArtifactContent.HIGHLIGHTED_TEXT, "testHighlightedText")
                .set("testArtifactContentExtra", "testArtifactContentExtraValue");
        row.addColumnFamily(contentColumnFamily);

        ColumnFamily genericMetadataColumnFamily = new ColumnFamily(ArtifactGenericMetadata.NAME);
        genericMetadataColumnFamily
                .set(ArtifactGenericMetadata.AUTHOR, "testAuthor")
                .set(ArtifactGenericMetadata.CHARSET, "testCharset")
                .set(ArtifactGenericMetadata.CATEGORY, "testCategory")
                .set(ArtifactGenericMetadata.DOCUMENT_DTG, "testDocumentDtg")
                .set(ArtifactGenericMetadata.DOCUMENT_TYPE, "testDocumentType")
                .set(ArtifactGenericMetadata.EXTRACTED_TEXT_HDFS_PATH, "testExtractedTextHdfsPath")
                .set(ArtifactGenericMetadata.EXTERNAL_URL, "testExternalUrl")
                .set(ArtifactGenericMetadata.FILE_EXTENSION, "testFileExtension")
                .set(ArtifactGenericMetadata.FILE_NAME, "testFileName")
                .set(ArtifactGenericMetadata.FILE_SIZE, 111L)
                .set(ArtifactGenericMetadata.FILE_TIMESTAMP, 222L)
                .set(ArtifactGenericMetadata.HDFS_FILE_PATH, "testHdfsFilePath")
                .set(ArtifactGenericMetadata.LANGUAGE, "testLanguage")
                .set(ArtifactGenericMetadata.LOAD_TIMESTAMP, 333L)
                .set(ArtifactGenericMetadata.LOAD_TYPE, "testLoadType")
                .set(ArtifactGenericMetadata.MIME_TYPE, "testMimeType")
                .set(ArtifactGenericMetadata.SOURCE, "testSource")
                .set(ArtifactGenericMetadata.SOURCE_SUBTYPE, "testSourceSubtype")
                .set(ArtifactGenericMetadata.SOURCE_TYPE, "testSourceType")
                .set(ArtifactGenericMetadata.SUBJECT, "testSubject")
                .set("testGenericMetadataExtra", "testGenericMetadataExtraValue");
        row.addColumnFamily(genericMetadataColumnFamily);

        ColumnFamily dynamicMetadataColumnFamily = new ColumnFamily(ArtifactDynamicMetadata.NAME);
        dynamicMetadataColumnFamily
                .set(ArtifactDynamicMetadata.ARTIFACT_SERIAL_NUMBER, "testArtifactSerialNumber")
                .set(ArtifactDynamicMetadata.ATTACHMENT, "{ \"test\": \"attachment\" }")
                .set(ArtifactDynamicMetadata.DOC_SOURCE_HASH, "testDocSourceHash")
                .set(ArtifactDynamicMetadata.EDH_GUID, "testEdhGuid")
                .set(ArtifactDynamicMetadata.GEO_LOCATION, "testGeolocation")
                .set(ArtifactDynamicMetadata.PROVENANCE_ID, "testProvenanceId")
                .set(ArtifactDynamicMetadata.SOURCE_HASH_ALGORITHM, "testSourceHashAlgorithm")
                .set(ArtifactDynamicMetadata.SOURCE_LABEL, "testSourceLabel")
                .set(ArtifactDynamicMetadata.STRUCTURED_ANNOTATION_OBJECT, "testStructuredAnnotationObject")
                .set(ArtifactDynamicMetadata.UNSTRUCTURED_ANNOTATION_OBJECT, "testUnstructuredAnnotationObject")
                .set("testDynamicMetadataExtra", "testDynamicMetadataExtraValue");
        row.addColumnFamily(dynamicMetadataColumnFamily);

        ColumnFamily extraColumnFamily = new ColumnFamily("testExtraColumnFamily");
        extraColumnFamily
                .set("testExtraColumn", "testExtraValue");
        row.addColumnFamily(extraColumnFamily);

        session.tables.get(Artifact.TABLE_NAME).add(row);

        Artifact artifact = artifactRepository.findByRowKey("key1", new User());
        assertEquals("key1", artifact.getRowKey().toString());

        assertEquals("testDocArtifactBytes", new String(artifact.getContent().getDocArtifactBytes()));
        assertEquals("testDocExtractedText", new String(artifact.getContent().getDocExtractedText()));
        assertEquals("testSecurity", artifact.getContent().getSecurity());
        assertEquals("testHighlightedText", artifact.getContent().getHighlightedText());
        assertEquals("testArtifactContentExtraValue", artifact.getContent().get("testArtifactContentExtra").toString());

        assertEquals("testAuthor", artifact.getGenericMetadata().getAuthor());
        assertEquals("testCharset", artifact.getGenericMetadata().getCharset());
        assertEquals("testCategory", artifact.getGenericMetadata().getCategory());
        assertEquals("testDocumentDtg", artifact.getGenericMetadata().getDocumentDtg());
        assertEquals("testDocumentType", artifact.getGenericMetadata().getDocumentType());
        assertEquals("testExtractedTextHdfsPath", artifact.getGenericMetadata().getExtractedTextHdfsPath());
        assertEquals("testExternalUrl", artifact.getGenericMetadata().getExternalUrl());
        assertEquals("testFileExtension", artifact.getGenericMetadata().getFileExtension());
        assertEquals("testFileName", artifact.getGenericMetadata().getFileName());
        assertEquals(111L, artifact.getGenericMetadata().getFileSize().longValue());
        assertEquals(222L, artifact.getGenericMetadata().getFileTimestamp().longValue());
        assertEquals("testHdfsFilePath", artifact.getGenericMetadata().getHdfsFilePath());
        assertEquals("testLanguage", artifact.getGenericMetadata().getLanguage());
        assertEquals(333L, artifact.getGenericMetadata().getLoadTimestamp().longValue());
        assertEquals("testLoadType", artifact.getGenericMetadata().getLoadType());
        assertEquals("testMimeType", artifact.getGenericMetadata().getMimeType());
        assertEquals("testSource", artifact.getGenericMetadata().getSource());
        assertEquals("testSourceSubtype", artifact.getGenericMetadata().getSourceSubtype());
        assertEquals("testSourceType", artifact.getGenericMetadata().getSourceType());
        assertEquals("testSubject", artifact.getGenericMetadata().getSubject());
        assertEquals("testGenericMetadataExtraValue", artifact.getGenericMetadata().get("testGenericMetadataExtra").toString());

        assertEquals("testArtifactSerialNumber", artifact.getDynamicMetadata().getArtifactSerialNumber());
        assertEquals("{ \"test\": \"attachment\" }", artifact.getDynamicMetadata().getAttachment());
        assertEquals("testDocSourceHash", artifact.getDynamicMetadata().getDocSourceHash());
        assertEquals("testEdhGuid", artifact.getDynamicMetadata().getEdhGuid());
        assertEquals("testGeolocation", artifact.getDynamicMetadata().getGeoLocation());
        assertEquals("testProvenanceId", artifact.getDynamicMetadata().getProvenanceId());
        assertEquals("testSourceHashAlgorithm", artifact.getDynamicMetadata().getSourceHashAlgorithm());
        assertEquals("testSourceLabel", artifact.getDynamicMetadata().getSourceLabel());
        assertEquals("testStructuredAnnotationObject", artifact.getDynamicMetadata().getStructuredAnnotationObject());
        assertEquals("testUnstructuredAnnotationObject", artifact.getDynamicMetadata().getUnstructuredAnnotationObject());
        assertEquals("testDynamicMetadataExtraValue", artifact.getDynamicMetadata().get("testDynamicMetadataExtra").toString());

        ColumnFamily foundExtraColumnFamily = artifact.get("testExtraColumnFamily");
        assertNotNull("foundExtraColumnFamily", foundExtraColumnFamily);
        assertEquals("testExtraValue", foundExtraColumnFamily.get("testExtraColumn").toString());
    }

    @Test
    public void testSave() {
        Artifact artifact = new Artifact();
        artifact.getContent()
                .setDocArtifactBytes("testDocArtifactBytes".getBytes())
                .setDocExtractedText("testDocExtractedText".getBytes())
                .setSecurity("testSecurity")
                .setHighlightedText("testHighlightedText")
                .set("contentExtra", "contentExtraValue");
        artifact.getGenericMetadata()
                .setAuthor("testAuthor")
                .setCharset("testCharset")
                .setCategory("testCategory")
                .setDocumentDtg("testDocumentDtg")
                .setDocumentType("testDocumentType")
                .setExtractedTextHdfsPath("testExtractedTextHdfsPath")
                .setExternalUrl("testExternalUrl")
                .setFileExtension("testFileExtension")
                .setFileName("testFileName")
                .setFileSize(111L)
                .setFileTimestamp(222L)
                .setHdfsFilePath("testHdfsFilePath")
                .setLanguage("testLanguage")
                .setLoadTimestamp(333L)
                .setLoadType("testLoadType")
                .setMimeType("testMimeType")
                .setSource("testSource")
                .setSourceSubtype("testSourceSubtype")
                .setSourceType("testSourceType")
                .setSubject("testSubject")
                .set("genericMetadataExtra", "genericMetadataExtraValue");
        artifact.getDynamicMetadata()
                .setArtifactSerialNumber("testArtifactSerialNumber")
                .setAttachment("{ \"test\": \"attachment\" }")
                .setDocSourceHash("testDocSourceHash")
                .setEdhGuid("testEdhGuid")
                .setGeolocation("testGeolocation")
                .setProvenanceId("testProvenanceId")
                .setSourceHashAlgorithm("testSourceHashAlgorithm")
                .setSourceLabel("testSourceLabel")
                .setStructuredAnnotationObject("testStructuredAnnotationObject")
                .setUnstructuredAnnotationObject("testUnstructuredAnnotationObject")
                .set("dynamicMetadataExtra", "dynamicMetadataExtraValue");
        artifact.addColumnFamily(
                new ColumnFamily("testExtraColumnFamily")
                        .set("testExtraColumn", "testExtraValue"));

        artifactRepository.save(artifact);

        assertEquals(1, session.tables.get(Artifact.TABLE_NAME).size()); // includes the dbpedia artifact
        Row row = session.tables.get(Artifact.TABLE_NAME).get(0);
        assertEquals(4, row.getColumnFamilies().size());

        assertEquals("urn\u001Fsha256\u001F69a7d91207b30df0240271bd16e7878f9559d54a480acc967b99917abc0301f9", row.getRowKey().toString());

        ColumnFamily contentColumnFamily = row.get(ArtifactContent.NAME);
        assertNotNull("contentColumnFamily", contentColumnFamily);
        assertEquals(5, contentColumnFamily.getColumns().size());
        assertEquals("testDocArtifactBytes", contentColumnFamily.get(ArtifactContent.DOC_ARTIFACT_BYTES).toString());
        assertEquals("testDocExtractedText", contentColumnFamily.get(ArtifactContent.DOC_EXTRACTED_TEXT).toString());
        assertEquals("testSecurity", contentColumnFamily.get(ArtifactContent.SECURITY).toString());
        assertEquals("testHighlightedText", contentColumnFamily.get(ArtifactContent.HIGHLIGHTED_TEXT).toString());
        assertEquals("contentExtraValue", contentColumnFamily.get("contentExtra").toString());

        ColumnFamily genericMetadataColumnFamily = row.get(ArtifactGenericMetadata.NAME);
        assertNotNull("genericMetadataColumnFamily", genericMetadataColumnFamily);
        assertEquals(21, genericMetadataColumnFamily.getColumns().size());
        assertEquals("testAuthor", genericMetadataColumnFamily.get(ArtifactGenericMetadata.AUTHOR).toString());
        assertEquals("testCharset", genericMetadataColumnFamily.get(ArtifactGenericMetadata.CHARSET).toString());
        assertEquals("testCategory", genericMetadataColumnFamily.get(ArtifactGenericMetadata.CATEGORY).toString());
        assertEquals("testDocumentDtg", genericMetadataColumnFamily.get(ArtifactGenericMetadata.DOCUMENT_DTG).toString());
        assertEquals("testDocumentType", genericMetadataColumnFamily.get(ArtifactGenericMetadata.DOCUMENT_TYPE).toString());
        assertEquals("testExtractedTextHdfsPath", genericMetadataColumnFamily.get(ArtifactGenericMetadata.EXTRACTED_TEXT_HDFS_PATH).toString());
        assertEquals("testExternalUrl", genericMetadataColumnFamily.get(ArtifactGenericMetadata.EXTERNAL_URL).toString());
        assertEquals("testFileExtension", genericMetadataColumnFamily.get(ArtifactGenericMetadata.FILE_EXTENSION).toString());
        assertEquals("testFileName", genericMetadataColumnFamily.get(ArtifactGenericMetadata.FILE_NAME).toString());
        assertEquals(111L, genericMetadataColumnFamily.get(ArtifactGenericMetadata.FILE_SIZE).toLong().longValue());
        assertEquals(222L, genericMetadataColumnFamily.get(ArtifactGenericMetadata.FILE_TIMESTAMP).toLong().longValue());
        assertEquals("testHdfsFilePath", genericMetadataColumnFamily.get(ArtifactGenericMetadata.HDFS_FILE_PATH).toString());
        assertEquals("testLanguage", genericMetadataColumnFamily.get(ArtifactGenericMetadata.LANGUAGE).toString());
        assertEquals(333L, genericMetadataColumnFamily.get(ArtifactGenericMetadata.LOAD_TIMESTAMP).toLong().longValue());
        assertEquals("testLoadType", genericMetadataColumnFamily.get(ArtifactGenericMetadata.LOAD_TYPE).toString());
        assertEquals("testMimeType", genericMetadataColumnFamily.get(ArtifactGenericMetadata.MIME_TYPE).toString());
        assertEquals("testSource", genericMetadataColumnFamily.get(ArtifactGenericMetadata.SOURCE).toString());
        assertEquals("testSourceSubtype", genericMetadataColumnFamily.get(ArtifactGenericMetadata.SOURCE_SUBTYPE).toString());
        assertEquals("testSourceType", genericMetadataColumnFamily.get(ArtifactGenericMetadata.SOURCE_TYPE).toString());
        assertEquals("testSubject", genericMetadataColumnFamily.get(ArtifactGenericMetadata.SUBJECT).toString());
        assertEquals("genericMetadataExtraValue", genericMetadataColumnFamily.get("genericMetadataExtra").toString());

        ColumnFamily dynamicMetadataColumnFamily = row.get(ArtifactDynamicMetadata.NAME);
        assertNotNull("dynamicMetadataColumnFamily", dynamicMetadataColumnFamily);
        assertEquals(11, dynamicMetadataColumnFamily.getColumns().size());
        assertEquals("testArtifactSerialNumber", dynamicMetadataColumnFamily.get(ArtifactDynamicMetadata.ARTIFACT_SERIAL_NUMBER).toString());
        assertEquals("{ \"test\": \"attachment\" }", dynamicMetadataColumnFamily.get(ArtifactDynamicMetadata.ATTACHMENT).toString());
        assertEquals("testDocSourceHash", dynamicMetadataColumnFamily.get(ArtifactDynamicMetadata.DOC_SOURCE_HASH).toString());
        assertEquals("testEdhGuid", dynamicMetadataColumnFamily.get(ArtifactDynamicMetadata.EDH_GUID).toString());
        assertEquals("testGeolocation", dynamicMetadataColumnFamily.get(ArtifactDynamicMetadata.GEO_LOCATION).toString());
        assertEquals("testProvenanceId", dynamicMetadataColumnFamily.get(ArtifactDynamicMetadata.PROVENANCE_ID).toString());
        assertEquals("testSourceHashAlgorithm", dynamicMetadataColumnFamily.get(ArtifactDynamicMetadata.SOURCE_HASH_ALGORITHM).toString());
        assertEquals("testSourceLabel", dynamicMetadataColumnFamily.get(ArtifactDynamicMetadata.SOURCE_LABEL).toString());
        assertEquals("testStructuredAnnotationObject", dynamicMetadataColumnFamily.get(ArtifactDynamicMetadata.STRUCTURED_ANNOTATION_OBJECT).toString());
        assertEquals("testUnstructuredAnnotationObject", dynamicMetadataColumnFamily.get(ArtifactDynamicMetadata.UNSTRUCTURED_ANNOTATION_OBJECT).toString());
        assertEquals("dynamicMetadataExtraValue", dynamicMetadataColumnFamily.get("dynamicMetadataExtra").toString());

        ColumnFamily extraColumnFamily = row.get("testExtraColumnFamily");
        assertNotNull("extraColumnFamily", extraColumnFamily);
        assertEquals(1, extraColumnFamily.getColumns().size());
        assertEquals("testExtraValue", extraColumnFamily.get("testExtraColumn").toString());
    }

    @Test
    public void testArtifactTypeUsingContentType() {
        Artifact artifact = new Artifact();

        artifact.getGenericMetadata().setMimeType("video");
        assertEquals("video", artifact.getType().toString());

        artifact.getGenericMetadata().setMimeType("text/html");
        assertEquals("document", artifact.getType().toString());

        artifact.getGenericMetadata().setMimeType("application.mp4");
        assertEquals("video", artifact.getType().toString());
    }
}
