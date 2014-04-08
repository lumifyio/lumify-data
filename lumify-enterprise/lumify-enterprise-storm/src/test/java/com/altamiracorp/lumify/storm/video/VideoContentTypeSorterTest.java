package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.ContentTypeSorter;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VideoContentTypeSorterTest {
    private ContentTypeSorter sorter;
    @Mock
    private ArchiveEntry entry;

    @Test
    public void testGetQueueNameFromMimeType() throws Exception {
        sorter = new VideoContentTypeSorter();
        String result = sorter.getQueueNameFromMimeType("video");
        assertEquals(VideoContentTypeSorter.QUEUE_NAME, result);
        assertNull(sorter.getQueueNameFromMimeType("image"));

    }

    @Test
    public void testGetQueueNameFromArchiveEntry() throws Exception {
        sorter = new VideoContentTypeSorter();
        when(entry.getName())
                .thenReturn("test" + VideoContentTypeSorter.SRT_CC_FILE_NAME_SUFFIX)
                .thenReturn("test" + VideoContentTypeSorter.YOUTUBE_CC_FILE_NAME_SUFFIX)
                .thenReturn("test" + VideoContentTypeSorter.YOUTUBE_CC_FILE_NAME_SUFFIX)
                .thenReturn("test" + VideoContentTypeSorter.SRT_CC_FILE_NAME_SUFFIX + ".txt");
        assertEquals(VideoContentTypeSorter.QUEUE_NAME, sorter.getQueueNameFromArchiveEntry(entry, null));
        assertEquals(VideoContentTypeSorter.QUEUE_NAME, sorter.getQueueNameFromArchiveEntry(entry, null));
        assertNull(sorter.getQueueNameFromArchiveEntry(entry, null));

    }
}


