package com.altamiracorp.lumify.storm.structuredDataExtraction;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ServiceLoader;

import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class StructuredDataBoltTest {
    private StructuredDataBolt bolt;
    @Before
    public void before() {
        bolt = new StructuredDataBolt();
    }

    @Test
    public void testGetThreadPrefix() {
        assertEquals("structuredDataBoltWorker", bolt.getThreadPrefix());
    }

    @Test
    public void  testGetServiceLoader() {
          assertEquals(ServiceLoader.load(StructuredDataExtractorWorker.class), bolt.getServiceLoader());
    }

}
