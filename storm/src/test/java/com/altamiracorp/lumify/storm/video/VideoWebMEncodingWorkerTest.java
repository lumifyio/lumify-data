package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VideoWebMEncodingWorkerTest {
    @Mock
    private FileSystem hdfsFileSystem;

    @Mock
    private FSDataOutputStream mockOutputStream;

    @Test
    public void testDoWork() throws Exception {
        VideoWebMEncodingWorker worker = new VideoWebMEncodingWorker();
        InputStream work = getClass().getResourceAsStream("test.mp4");
        AdditionalArtifactWorkData data = new AdditionalArtifactWorkData();
        data.setFileName("test.mp4");
        data.setHdfsFileSystem(hdfsFileSystem);

        ArgumentCaptor<Path> argument = ArgumentCaptor.forClass(Path.class);
        when(hdfsFileSystem.create(argument.capture())).thenReturn(mockOutputStream);

        ArtifactExtractedInfo result = worker.doWork(work, data);
        assertNotNull(result);
        assertTrue("Mp4HdfsFilePath did not contain with hdfsLimitOutputStream: " + result.getMp4HdfsFilePath(), result.getMp4HdfsFilePath().contains("hdfsLimitOutputStream"));
    }
}
