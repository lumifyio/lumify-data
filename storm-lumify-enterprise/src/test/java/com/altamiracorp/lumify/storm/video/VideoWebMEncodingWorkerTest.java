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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
        InputStream work = null;
        AdditionalArtifactWorkData data = new AdditionalArtifactWorkData();
        data.setLocalFileName(getClass().getResource("test.mp4").getFile());
        data.setHdfsFileSystem(hdfsFileSystem);

        ArgumentCaptor<Path> argument = ArgumentCaptor.forClass(Path.class);
        when(hdfsFileSystem.create(argument.capture())).thenReturn(mockOutputStream);

        ArtifactExtractedInfo result = worker.doWork(work, data);
        assertNotNull(result);
        assertTrue("WebMHdfsFilePath did not contain with hdfsLimitOutputStream: " + result.getWebMHdfsFilePath(), result.getWebMHdfsFilePath().contains("hdfsLimitOutputStream"));
    }
}
