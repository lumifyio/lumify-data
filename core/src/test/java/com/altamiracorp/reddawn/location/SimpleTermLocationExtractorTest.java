package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.model.geoNames.GeoName;
import com.altamiracorp.reddawn.model.geoNames.GeoNameMetadata;
import com.altamiracorp.reddawn.model.geoNames.GeoNameRepository;
import com.altamiracorp.reddawn.model.workspace.WorkspaceRepository;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class SimpleTermLocationExtractorTest {
    private MockSession session;
    private WorkspaceRepository workspaceRepository;

    @Before
    public void before() {
        session = new MockSession();
        session.initializeTables();
        workspaceRepository = new WorkspaceRepository();
    }

    @Test
    public void testLookupReturnsHighestPopulation() throws Exception {
        GeoNameRepository geoNameRepository = new GeoNameRepository();
        addGeoName("baltimore", 1, 100L, 77.1, -51.1);
        addGeoName("baltimore", 2, 300L, 87.1, -61.1);
        addGeoName("baltimore", 3, 200L, 97.1, -71.1);
        SimpleTermLocationExtractor simpleTermLocationExtractor = new SimpleTermLocationExtractor();
        Term termIn = new Term("baltimore", "CDC", "Location");
        termIn.addTermMention(new TermMention().setArtifactKey("artifact1"));
        termIn.addTermMention(new TermMention().setArtifactKey("artifact2"));
        Term termOut = simpleTermLocationExtractor.GetTermWithLocationLookup(session, geoNameRepository, termIn);
        assertNotNull(termOut);
        for (TermMention termMention : termOut.getTermMentions()) {
            assertEquals("POINT(87.1,-61.1)", termMention.getGeoLocation());
        }
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
