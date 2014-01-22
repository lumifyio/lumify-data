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
import com.altamiracorp.securegraph.Vertex;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TweetFinalizerBoltTest extends BaseTwitterBoltTest<TweetFinalizerBolt> {
    @Override
    protected TweetFinalizerBolt createBolt() {
        return new TweetFinalizerBolt();
    }
    
    @Test
    public void testDeclareOutputFields() {
        OutputFieldsDeclarer ofd = mock(OutputFieldsDeclarer.class);
        instance.declareOutputFields(ofd);
        verify(ofd, never()).declare(any(Fields.class));
    }
    
    @Test
    public void testProcessJson_NoTweetVertex() throws Exception {
        Tuple tuple = mock(Tuple.class);
        JSONObject json = mock(JSONObject.class);

        when(tuple.getValueByField(TwitterStormConstants.TWEET_VERTEX_FIELD)).thenReturn(null);
        
        instance.processJson(json, tuple);
        verify(twitterProcessor, never()).finalizeTweetVertex(anyString(), anyString());
    }
    
    @Test
    public void testProcessJson_WithTweetVertex() throws Exception {
        Tuple tuple = mock(Tuple.class);
        Vertex tweetVertex = mock(Vertex.class);
        String vertexId = "testId";
        JSONObject json = mock(JSONObject.class);

        when(tuple.getValueByField(TwitterStormConstants.TWEET_VERTEX_FIELD)).thenReturn(tweetVertex);
        when(tweetVertex.getId()).thenReturn(vertexId);
        
        instance.processJson(json, tuple);
        verify(twitterProcessor).finalizeTweetVertex(anyString(), eq(vertexId));
    }
}
