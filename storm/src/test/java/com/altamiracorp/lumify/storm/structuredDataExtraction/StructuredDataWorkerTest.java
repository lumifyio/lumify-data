package com.altamiracorp.lumify.storm.structuredDataExtraction;

import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class StructuredDataWorkerTest {

    private StructuredDataExtractorWorker worker;
    private Artifact artifact;
    @Before
    public void before() {
        worker = new StructuredDataExtractorWorker();
        artifact = mock(Artifact.class);
    }

    @Test
    public void testPrepare() {

    }

    @Test
    public void testDoWork() {
    }


}
