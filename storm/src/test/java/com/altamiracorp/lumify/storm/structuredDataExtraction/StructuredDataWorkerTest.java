package com.altamiracorp.lumify.storm.structuredDataExtraction;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactMetadata;
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
        data.setMimeType("text/csv");

    }

    @Test
    public void testPrepare() {

    }

    @Test
    public void testDoWork() {
        try {
        ArtifactExtractedInfo result = worker.doWork(stream, data);


        }  catch (Exception e) {
           fail();
        }
    }


}
