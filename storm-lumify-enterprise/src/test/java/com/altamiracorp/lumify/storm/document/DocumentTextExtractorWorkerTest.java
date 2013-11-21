package com.altamiracorp.lumify.storm.document;


import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.textExtraction.TikaTextExtractor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class DocumentTextExtractorWorkerTest {
    @Mock
    com.altamiracorp.lumify.textExtraction.TikaTextExtractor extractor;
    private DocumentTextExtractorWorker worker;
    private AdditionalArtifactWorkData data;
    private ArtifactExtractedInfo info;

    @Before
    public void setup() throws Exception {
        worker = new DocumentTextExtractorWorker();
        FileSystem hdfsFileSystem = FileSystem.get(new Configuration());
        data = new AdditionalArtifactWorkData();
        data.setMimeType("text/plain");
        data.setHdfsFileSystem(hdfsFileSystem);
        info = new ArtifactExtractedInfo();
        worker.setTikaTextExtractor(new TikaTextExtractor());
    }

    @Test
    public void testDoWork() throws Exception {
        InputStream in = getClass().getResourceAsStream("bffls.txt");
        info = worker.doWork(in, data);
        assertEquals("\n" +
                "Joe and Sam are BFFLs. Joe and Sam works together. Sam is an intern at Near Infinity. \n" +
                "Joe is a full time employee at Near Infinity. Near Infinity was established in 2002. \n" +
                "\n", info.getText());
        assertNull(info.getTextHdfsPath());

    }

    @Test
    public void testDoWorkWithLargeFile() throws Exception {
        //todo add test with file that is too big for accumulo
        //tika has hard limit of 100k characters, and we put anything > 512kb into hdfs
        //how do we have a file with <100k characters but >512kb to test with?

    }

}
