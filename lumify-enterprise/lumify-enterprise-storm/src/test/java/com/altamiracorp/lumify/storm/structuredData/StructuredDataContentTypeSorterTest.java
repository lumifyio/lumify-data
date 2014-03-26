package com.altamiracorp.lumify.storm.structuredData;

import com.altamiracorp.lumify.core.contentType.ContentTypeSorter;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StructuredDataContentTypeSorterTest {
    private ContentTypeSorter sorter;
    @Mock
    ArchiveEntry entry;

    @Test
    public void testGetQueueNameFromMimeType() throws Exception {
        sorter = new StructuredDataContentTypeSorter();
        assertNull(sorter.getQueueNameFromMimeType(null));
    }

    @Test
    public void testGetQueueNameFromArchiveEntry() throws Exception {
        sorter = new StructuredDataContentTypeSorter();
        when(entry.getName())
                .thenReturn("test" + StructuredDataContentTypeSorter.MAPPING_JSON_FILE_NAME_SUFFIX)
                .thenReturn("test" + StructuredDataContentTypeSorter.MAPPING_JSON_FILE_NAME_SUFFIX + ".txt");
        assertEquals(StructuredDataContentTypeSorter.QUEUE_NAME, sorter.getQueueNameFromArchiveEntry(entry, null));
        assertNull(sorter.getQueueNameFromArchiveEntry(entry, null));
    }
}
