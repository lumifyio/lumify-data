package com.altamiracorp.reddawn.model.geoNames;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.model.workspace.WorkspaceContent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class GeoNameRepositoryTest {
    private MockSession session;
    private GeoNameRepository geoNameRepository;

    @Before
    public void before() {
        session = new MockSession();
        session.initializeTables();
        geoNameRepository = new GeoNameRepository();
    }

    @Test
    public void testFindByRowKey() {
        String rowKeyString = "boston:123";
        Row<RowKey> row = new Row<RowKey>(GeoName.TABLE_NAME, new RowKey(rowKeyString));

        ColumnFamily geoNameMetadataColumnFamily = new ColumnFamily(GeoNameMetadata.NAME);
        geoNameMetadataColumnFamily
                .set(GeoNameMetadata.NAME_COLUMN, "boston")
                .set(GeoNameMetadata.LATITUDE, 42.35)
                .set(GeoNameMetadata.LONGITUDE, -71.06)
                .set("extra", "textExtra");
        row.addColumnFamily(geoNameMetadataColumnFamily);

        ColumnFamily extraColumnFamily = new ColumnFamily("testExtraColumnFamily");
        extraColumnFamily
                .set("testExtraColumn", "testExtraValue");
        row.addColumnFamily(extraColumnFamily);

        session.tables.get(GeoName.TABLE_NAME).add(row);

        GeoName geoName = geoNameRepository.findByRowKey(session, rowKeyString);
        assertEquals(rowKeyString, geoName.getRowKey().toString());
        assertEquals(2, geoName.getColumnFamilies().size());

        GeoNameMetadata geoNameMetadata = geoName.getMetadata();
        assertEquals(GeoNameMetadata.NAME, geoNameMetadata.getColumnFamilyName());
        assertEquals("boston", geoNameMetadata.getName());
        assertEquals(42.35, geoNameMetadata.getLatitude().doubleValue(), 0.01);
        assertEquals(-71.06, geoNameMetadata.getLongitude().doubleValue(), 0.01);
        assertEquals("textExtra", geoNameMetadata.get("extra").toString());

        ColumnFamily foundExtraColumnFamily = geoName.get("testExtraColumnFamily");
        assertNotNull("foundExtraColumnFamily", foundExtraColumnFamily);
        assertEquals("testExtraValue", foundExtraColumnFamily.get("testExtraColumn").toString());
    }

    @Test
    public void testSave() {
        GeoName geoName = new GeoName("Boston", "123");

        geoName.getMetadata()
                .setName("Boston")
                .setLatitude(42.35)
                .setLongitude(-71.06)
                .set("testExtra", "testExtraValue");

        geoName.addColumnFamily(
                new ColumnFamily("testExtraColumnFamily")
                        .set("testExtraColumn", "testExtraValue"));

        geoNameRepository.save(session, geoName);

        assertEquals(1, session.tables.get(GeoName.TABLE_NAME).size());
        Row row = session.tables.get(GeoName.TABLE_NAME).get(0);
        assertEquals(RowKeyHelper.buildMinor("boston", "123"), row.getRowKey().toString());

        assertEquals(2, row.getColumnFamilies().size());

        ColumnFamily geoNameMetadataColumnFamily = row.get(GeoNameMetadata.NAME);
        assertEquals(GeoNameMetadata.NAME, geoNameMetadataColumnFamily.getColumnFamilyName());
        assertEquals("Boston", geoNameMetadataColumnFamily.get(GeoNameMetadata.NAME_COLUMN).toString());
        assertEquals(42.35, geoNameMetadataColumnFamily.get(GeoNameMetadata.LATITUDE).toDouble().doubleValue(), 0.01);
        assertEquals(-71.06, geoNameMetadataColumnFamily.get(GeoNameMetadata.LONGITUDE).toDouble().doubleValue(), 0.01);
        assertEquals("testExtraValue", geoNameMetadataColumnFamily.get("testExtra").toString());

        ColumnFamily extraColumnFamily = row.get("testExtraColumnFamily");
        assertNotNull("extraColumnFamily", extraColumnFamily);
        assertEquals(1, extraColumnFamily.getColumns().size());
        assertEquals("testExtraValue", extraColumnFamily.get("testExtraColumn").toString());
    }
}