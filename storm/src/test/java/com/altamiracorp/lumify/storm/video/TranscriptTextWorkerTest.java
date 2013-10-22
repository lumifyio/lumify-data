package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

public class TranscriptTextWorkerTest {
    TranscriptTextWorker worker;
    InputStream in;
    AdditionalArtifactWorkData data;

    @Before
    public void setup() {
        data = new AdditionalArtifactWorkData();
        worker = new TranscriptTextWorker();
    }

    @Test
    public void testYoutubeCC() throws Exception {
        in = getClass().getResourceAsStream("ytcc.lumify");
        File dir = new File(getClass().getResource("ytcc.lumify").getPath());
        data.setArchiveTempDir(dir);

        ArtifactExtractedInfo result = worker.doWork(in, data);
        checkNotNull(result.getVideoTranscript());
        List<VideoTranscript.TimedText> entries = result.getVideoTranscript().getEntries();
        assertEquals(4, entries.size());
        assertEquals("Well I woke up to go get me a cold pop", entries.get(0).getText());
        assertEquals("Then I thought somebody was barbecuing", entries.get(1).getText());
        assertEquals("I said O' lord Jesus It's a fire", entries.get(2).getText());
        assertEquals("Then I ran out, I didn't grab no shoes or nothing Jesus", entries.get(3).getText());
    }

    @Test
    public void testSrtCC() throws Exception {
        in = getClass().getResourceAsStream("test-video-with-transcript.lumify");
        File dir = new File(getClass().getResource("test-video-with-transcript.lumify").getPath());
        data.setArchiveTempDir(dir);

        ArtifactExtractedInfo result = worker.doWork(in, data);
        checkNotNull(result.getVideoTranscript());
        List<VideoTranscript.TimedText> entries = result.getVideoTranscript().getEntries();
        assertEquals(3, entries.size());
        assertEquals("Salam will", entries.get(0).getText());
        assertEquals("is an absolutely sickening and despicable attack", entries.get(1).getText());
        assertEquals("appalling brutality what we know is that three British nationals", entries.get(2).getText());
    }

    @Test
    public void testNoCC() throws Exception {
        in = getClass().getResourceAsStream("test.mp4");

        ArtifactExtractedInfo result = worker.doWork(in, data);
        checkNotNull(result.getVideoTranscript());
        List<VideoTranscript.TimedText> entries = result.getVideoTranscript().getEntries();
        assertEquals(0, entries.size());
    }
}
