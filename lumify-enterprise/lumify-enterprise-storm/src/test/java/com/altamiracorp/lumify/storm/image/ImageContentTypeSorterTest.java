package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.contentType.ContentTypeSorter;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class ImageContentTypeSorterTest {
    private ContentTypeSorter sorter;
    @Mock
    ArchiveEntry entry;
    @Mock
    InputStream in;

    @Test
    public void testGetQueueNameFromMimeType() throws Exception {
        sorter = new ImageContentTypeSorter();
        String result = sorter.getQueueNameFromMimeType("image");
        assertEquals("image", result);
        result = sorter.getQueueNameFromMimeType("imAgE");
        assertEquals("image", result);
        result = sorter.getQueueNameFromMimeType("somethingimage123");
        assertEquals("image", result);
        result = sorter.getQueueNameFromMimeType("imagNOTe");
        assertNull(result);
    }

    @Test
    public void testGetQueueNameFromArchiveEntry() throws Exception {
        sorter = new ImageContentTypeSorter();
        assertNull(sorter.getQueueNameFromArchiveEntry(entry, in));
    }
}
