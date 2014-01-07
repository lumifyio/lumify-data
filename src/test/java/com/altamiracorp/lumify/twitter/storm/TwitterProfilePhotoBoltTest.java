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

import static org.mockito.Mockito.*;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TwitterUserParsingBolt.class })
public class TwitterProfilePhotoBoltTest extends BaseTwitterBoltTest<TwitterProfilePhotoBolt> {
    @Override
    protected TwitterProfilePhotoBolt createBolt() {
        return new TwitterProfilePhotoBolt();
    }
    
    @Test
    public void testDeclareOutputFields() {
        OutputFieldsDeclarer ofd = mock(OutputFieldsDeclarer.class);
        instance.declareOutputFields(ofd);
        verify(ofd, never()).declare(any(Fields.class));
    }
    
    @Test
    public void testProcessJson_NoUserVertex() throws Exception {
        Tuple tuple = mock(Tuple.class);
        JSONObject json = mock(JSONObject.class);

        when(tuple.getValueByField(TwitterStormConstants.TWITTER_USER_VERTEX_FIELD)).thenReturn(null);
        
        instance.processJson(json, tuple);
        verify(twitterProcessor, never()).retrieveProfileImage(anyString(), any(JSONObject.class), any(GraphVertex.class));
    }
    
    @Test
    public void testProcessJson_WithUserVertex() throws Exception {
        Tuple tuple = mock(Tuple.class);
        GraphVertex tweeterVertex = mock(GraphVertex.class);
        JSONObject json = mock(JSONObject.class);

        when(tuple.getValueByField(TwitterStormConstants.TWITTER_USER_VERTEX_FIELD)).thenReturn(tweeterVertex);
        
        instance.processJson(json, tuple);
        verify(twitterProcessor).retrieveProfileImage(anyString(), eq(json), eq(tweeterVertex));
    }
}
