package com.altamiracorp.lumify.location;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.*;
import com.altamiracorp.lumify.model.geoNames.*;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.workspace.WorkspaceRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class SimpleTermLocationExtractorTest {
    private MockSession session;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private User user;

    @Mock
    private GeoNameRepository geoNameRepository;

    @Mock
    private GeoNameAdmin1CodeRepository geoNameAdmin1CodeRepository;

    @Mock
    private GeoNameCountryInfoRepository geoNameCountryInfoRepository;

    @Before
    public void before() {
        session = new MockSession();
        session.initializeTables(user);
    }

    @Test
    public void testLookupReturnsHighestPopulation() throws Exception {
        addGeoName("baltimore", 1, 100L, 77.1, -51.1);
        addGeoName("baltimore", 2, 300L, 87.1, -61.1);
        addGeoName("baltimore", 3, 200L, 97.1, -71.1);
        SimpleTermLocationExtractor simpleTermLocationExtractor = new SimpleTermLocationExtractor(geoNameAdmin1CodeRepository, geoNameCountryInfoRepository);
        TermMention termIn = new TermMention();
        termIn.getMetadata().setSign("baltimore");
        TermMention termOut = simpleTermLocationExtractor.GetTermWithLocationLookup(geoNameRepository, termIn, user);
        assertNotNull(termOut);
        assertEquals("POINT(87.1,-61.1)", termOut.getMetadata().getGeoLocation());
    }

    private void addGeoName(String rowKey, int id, Long population, Double latitude, Double longitude) {
        Row<RowKey> row = new Row<RowKey>(GeoName.TABLE_NAME, new RowKey(rowKey + RowKeyHelper.MINOR_FIELD_SEPARATOR + id));
        ColumnFamily geoNameMetadata = new ColumnFamily(GeoNameMetadata.NAME);
        geoNameMetadata.set(GeoNameMetadata.POPULATION, population);
        geoNameMetadata.set(GeoNameMetadata.LATITUDE, latitude);
        geoNameMetadata.set(GeoNameMetadata.LONGITUDE, longitude);
        row.addColumnFamily(geoNameMetadata);
        session.tables.get(GeoName.TABLE_NAME).add(row);
    }
}
