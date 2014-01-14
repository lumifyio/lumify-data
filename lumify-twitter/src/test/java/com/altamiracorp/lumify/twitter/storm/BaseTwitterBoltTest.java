/*
 * Copyright 2014 Altamira Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
