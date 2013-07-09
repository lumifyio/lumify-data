package com.altamiracorp.reddawn.ucd.source;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.MockSession;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SourceRepositoryTest {
    private MockSession session;
    private SourceRepository sourceRepository;

    @Before
    public void before() {
        session = new MockSession();
        session.initializeTables();
        sourceRepository = new SourceRepository();
    }

    @Test
    public void testFindByRowKey() {
        String rowKeyString = "testUuidRowKey";
        Row<RowKey> row = new Row<RowKey>(Source.TABLE_NAME, new RowKey(rowKeyString));

        ColumnFamily sourceMetaDataColumnFamily = new ColumnFamily(SourceMetadata.NAME);
        sourceMetaDataColumnFamily
                .set(SourceMetadata.ACRONYM, "testAcronym")
                .set(SourceMetadata.ADDITIONAL_DETAILS, "testAdditionalDetails")
                .set(SourceMetadata.COUNTRY, "testCountry")
                .set(SourceMetadata.DATA_TYPE, "testDataType")
                .set(SourceMetadata.DESCRIPTION, "testDescription")
                .set(SourceMetadata.INGEST_DATE, 111L)
                .set(SourceMetadata.INGEST_STATUS, "testIngestStatus")
                .set(SourceMetadata.INTEL_TYPES, "testIntelTypes")
                .set(SourceMetadata.LOCATION, "testLocation")
                .set(SourceMetadata.MEDIA_TYPES, "testMediaTypes")
                .set(SourceMetadata.NAME, "testName")
                .set(SourceMetadata.ORG_ACRONYM, "testOrgAcronym")
                .set(SourceMetadata.ORG_NAME, "testOrgName")
                .set(SourceMetadata.ORG_PARENT_ACRONYM, "testOrgParentAcronym")
                .set(SourceMetadata.ORG_PARENT_NAME, "testOrgParentName")
                .set(SourceMetadata.TYPE, "testType")
                .set("extra", "textExtra");
        row.addColumnFamily(sourceMetaDataColumnFamily);

        ColumnFamily extraColumnFamily = new ColumnFamily("testExtraColumnFamily");
        extraColumnFamily
                .set("testExtraColumn", "testExtraValue");
        row.addColumnFamily(extraColumnFamily);

        session.tables.get(Source.TABLE_NAME).add(row);

        Source source = sourceRepository.findByRowKey(session, rowKeyString);
        assertEquals(rowKeyString, source.getRowKey().toString());
        assertEquals(2, source.getColumnFamilies().size());

        SourceMetadata sourceMetadata = source.getMetadata();
        assertEquals(SourceMetadata.NAME, sourceMetadata.getColumnFamilyName());
        assertEquals("testAcronym", sourceMetadata.getAcronym());
        assertEquals("testAdditionalDetails", sourceMetadata.getAdditionalDetails());
        assertEquals("testCountry", sourceMetadata.getCountry());
        assertEquals("testDataType", sourceMetadata.getDataType());
        assertEquals("testDescription", sourceMetadata.getDescription());
        assertEquals(111L, sourceMetadata.getIngestDate().longValue());
        assertEquals("testIngestStatus", sourceMetadata.getIngestStatus());
        assertEquals("testIntelTypes", sourceMetadata.getIntelTypes());
        assertEquals("testLocation", sourceMetadata.getLocation());
        assertEquals("testMediaTypes", sourceMetadata.getMediaTypes());
        assertEquals("testName", sourceMetadata.getName());
        assertEquals("testOrgAcronym", sourceMetadata.getOrgAcronym());
        assertEquals("testOrgName", sourceMetadata.getOrgName());
        assertEquals("testOrgParentAcronym", sourceMetadata.getOrgParentAcronym());
        assertEquals("testOrgParentName", sourceMetadata.getOrgParentName());
        assertEquals("testType", sourceMetadata.getType());
        assertEquals("textExtra", sourceMetadata.get("extra").toString());

        ColumnFamily foundExtraColumnFamily = source.get("testExtraColumnFamily");
        assertNotNull("foundExtraColumnFamily", foundExtraColumnFamily);
        assertEquals("testExtraValue", foundExtraColumnFamily.get("testExtraColumn").toString());
    }

    @Test
    public void testSave() {
        Source source = new Source("testUuid");

        source.getMetadata()
                .setAcronym("testAcronym")
                .setAdditionalDetails("testAdditionalDetails")
                .setCountry("testCountry")
                .setDataType("testDataType")
                .setDescription("testDescription")
                .setIngestDate(111L)
                .setIngestStatus("testIngestStatus")
                .setIntelTypes("testIntelTypes")
                .setLocation("testLocation")
                .setMediaTypes("testMediaTypes")
                .setName("testName")
                .setOrgAcronym("testOrgAcronym")
                .setOrgName("testOrgName")
                .setOrgParentAcronym("testOrgParentAcronym")
                .setOrgParentName("testOrgParentName")
                .setType("testType")
                .set("testExtra", "testExtraValue");

        source.addColumnFamily(
                new ColumnFamily("testExtraColumnFamily")
                        .set("testExtraColumn", "testExtraValue"));

        sourceRepository.save(session, source);

        assertEquals(1, session.tables.get(Source.TABLE_NAME).size());
        Row row = session.tables.get(Source.TABLE_NAME).get(0);
        assertEquals("testUuid", row.getRowKey().toString());

        assertEquals(2, row.getColumnFamilies().size());

        ColumnFamily sourceMetadataColumnFamily = row.get(SourceMetadata.NAME);
        assertEquals(SourceMetadata.NAME, sourceMetadataColumnFamily.getColumnFamilyName());
        assertEquals("testAcronym", sourceMetadataColumnFamily.get(SourceMetadata.ACRONYM).toString());
        assertEquals("testAdditionalDetails", sourceMetadataColumnFamily.get(SourceMetadata.ADDITIONAL_DETAILS).toString());
        assertEquals("testCountry", sourceMetadataColumnFamily.get(SourceMetadata.COUNTRY).toString());
        assertEquals("testDataType", sourceMetadataColumnFamily.get(SourceMetadata.DATA_TYPE).toString());
        assertEquals("testDescription", sourceMetadataColumnFamily.get(SourceMetadata.DESCRIPTION).toString());
        assertEquals(111L, sourceMetadataColumnFamily.get(SourceMetadata.INGEST_DATE).toLong().longValue());
        assertEquals("testIngestStatus", sourceMetadataColumnFamily.get(SourceMetadata.INGEST_STATUS).toString());
        assertEquals("testIntelTypes", sourceMetadataColumnFamily.get(SourceMetadata.INTEL_TYPES).toString());
        assertEquals("testLocation", sourceMetadataColumnFamily.get(SourceMetadata.LOCATION).toString());
        assertEquals("testMediaTypes", sourceMetadataColumnFamily.get(SourceMetadata.MEDIA_TYPES).toString());
        assertEquals("testName", sourceMetadataColumnFamily.get(SourceMetadata.NAME).toString());
        assertEquals("testOrgAcronym", sourceMetadataColumnFamily.get(SourceMetadata.ORG_ACRONYM).toString());
        assertEquals("testOrgName", sourceMetadataColumnFamily.get(SourceMetadata.ORG_NAME).toString());
        assertEquals("testOrgParentAcronym", sourceMetadataColumnFamily.get(SourceMetadata.ORG_PARENT_ACRONYM).toString());
        assertEquals("testOrgParentName", sourceMetadataColumnFamily.get(SourceMetadata.ORG_PARENT_NAME).toString());
        assertEquals("testType", sourceMetadataColumnFamily.get(SourceMetadata.TYPE).toString());
        assertEquals("testExtraValue", sourceMetadataColumnFamily.get("testExtra").toString());

        ColumnFamily extraColumnFamily = row.get("testExtraColumnFamily");
        assertNotNull("extraColumnFamily", extraColumnFamily);
        assertEquals(1, extraColumnFamily.getColumns().size());
        assertEquals("testExtraValue", extraColumnFamily.get("testExtraColumn").toString());
    }
}
