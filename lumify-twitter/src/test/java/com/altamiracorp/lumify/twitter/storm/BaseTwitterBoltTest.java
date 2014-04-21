package com.altamiracorp.lumify.twitter.storm;

import static org.junit.Assert.*;

import backtype.storm.task.OutputCollector;
import com.altamiracorp.lumify.twitter.LumifyTwitterProcessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

public abstract class BaseTwitterBoltTest<B extends BaseTwitterBolt> {
    @Mock
    protected LumifyTwitterProcessor twitterProcessor;
    
    @Mock
    protected OutputCollector outputCollector;
    
    protected B instance;
    
    protected abstract B createBolt();
    
    @Before
    public final void setUp() throws Exception {
        instance = PowerMockito.spy(createBolt());
        instance.setTwitterProcessor(twitterProcessor);
        Whitebox.setInternalState(instance, OutputCollector.class, outputCollector);
    }
    
    @Test
    public void testGetSetTwitterProcessor() {
        assertEquals(twitterProcessor, instance.getTwitterProcessor());
    }
}
