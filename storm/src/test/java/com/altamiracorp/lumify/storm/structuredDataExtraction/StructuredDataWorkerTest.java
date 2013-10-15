package com.altamiracorp.lumify.storm.structuredDataExtraction;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class StructuredDataWorkerTest {

    private StructuredDataExtractorWorker worker;
    private InputStream stream;
    private AdditionalArtifactWorkData data;
    @Before
    public void before() {
        worker = new StructuredDataExtractorWorker();
        stream = getClass().getResourceAsStream("personLocations.csv");
        data = new AdditionalArtifactWorkData();
        data.setMimeType("text/plain");

    }

    @Test
    public void testPrepare() {

    }

    @Test
    public void testDoWork() {
        try {
        ArtifactExtractedInfo result = worker.doWork(stream, data);
        assertEquals("Name,Zip Code\nJoe Ferner,20147,10/30/1977,blah\n", result.getText() );

        }  catch (Exception e) {
           System.out.println(e.toString());
           fail();
        }
    }


}
