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

import static com.altamiracorp.lumify.storm.util.FieldsMatcher.*;
import static org.mockito.Mockito.*;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.storm.BaseLumifyJsonBolt;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TwitterUserParsingBolt.class })
public class TwitterUserParsingBoltTest extends BaseTwitterBoltTest<TwitterUserParsingBolt> {
    @Override
    protected TwitterUserParsingBolt createBolt() {
        return new TwitterUserParsingBolt();
    }
    
    @Test
    public void testDeclareOutputFields() {
        OutputFieldsDeclarer ofd = mock(OutputFieldsDeclarer.class);
        instance.declareOutputFields(ofd);
        verify(ofd).declare(argThat(sameFields(BaseLumifyJsonBolt.JSON_FIELD, TwitterStormConstants.TWITTER_USER_VERTEX_FIELD)));
    }
    
    @Test
    public void testProcessJson_NoUser() throws Exception {
        Tuple tuple = mock(Tuple.class);
        GraphVertex tweetVertex = mock(GraphVertex.class);
        JSONObject json = mock(JSONObject.class);

        when(tuple.getValueByField(TwitterStormConstants.TWEET_VERTEX_FIELD)).thenReturn(tweetVertex);
        when(twitterProcessor.parseTwitterUser(anyString(), eq(json), eq(tweetVertex))).thenReturn(null);
        
        instance.processJson(json, tuple);
        verify(outputCollector, never()).emit(any(Tuple.class), any(List.class));
    }
    
    @Test
    public void testProcessJson_WithUser() throws Exception {
        Tuple tuple = mock(Tuple.class);
        GraphVertex tweetVertex = mock(GraphVertex.class);
        GraphVertex tweeterVertex = mock(GraphVertex.class);
        JSONObject json = mock(JSONObject.class);
        String jsonStr = "{\"foo\": \"bar\"}";

        when(json.toString()).thenReturn(jsonStr);
        when(tuple.getValueByField(TwitterStormConstants.TWEET_VERTEX_FIELD)).thenReturn(tweetVertex);
        when(twitterProcessor.parseTwitterUser(anyString(), eq(json), eq(tweetVertex))).thenReturn(tweeterVertex);
        
        instance.processJson(json, tuple);
        verify(outputCollector).emit(tuple, Arrays.asList(jsonStr, tweeterVertex));
    }
}
