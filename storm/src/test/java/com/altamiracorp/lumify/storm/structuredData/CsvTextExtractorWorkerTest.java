package com.altamiracorp.lumify.storm.structuredData;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CsvTextExtractorWorkerTest {

    private CsvTextExtractorWorker worker;
    private InputStream stream;
    private AdditionalArtifactWorkData data;
    @Mock
    private FileSystem hdfsFileSystem;
    @Mock
    private FSDataOutputStream mockOutputStream;
    @Mock
    private FSDataInputStream mockMapping;

    @Test
    public void testDoWork() throws Exception {

        worker = new CsvTextExtractorWorker();
        stream = getClass().getResourceAsStream("personLocations.tar");
        data = new AdditionalArtifactWorkData();
        data.setMimeType("application/x-tar");
        data.setHdfsFileSystem(hdfsFileSystem);
        FileSystem fs = FileSystem.get(new Configuration());
        URL mockMappingUrl = getClass().getResource("personLocations.csv.mapping.json");
        URL mockCSVUrl = getClass().getResource("personLocations.csv");

        when(hdfsFileSystem.create(any(Path.class), anyBoolean())).thenReturn(mockOutputStream);
        ArgumentCaptor<Path> argument = ArgumentCaptor.forClass(Path.class);
        when(data.getHdfsFileSystem().open(argument.capture()))
                .thenReturn(fs.open(new Path(mockMappingUrl.toString())))
                .thenReturn(fs.open(new Path(mockCSVUrl.toString())));
        ArtifactExtractedInfo result = worker.doWork(stream, data);

        //Test that the text was imported correctly
        assertEquals("Name,Zip Code\nJoe Ferner,20147,10/30/1977,blah\n", result.getText());
        //Title should be the "subject" field within the mapping
        assertEquals("People Zip Codes", result.getTitle());
        //Check that we put the right thing into the mapping field
        assertEquals(new JSONObject(IOUtils.toString(getClass().getResourceAsStream("personLocations.csv.mapping.json"))).toString()
                , new JSONObject(result.getMappingJson()).toString());
    }


}