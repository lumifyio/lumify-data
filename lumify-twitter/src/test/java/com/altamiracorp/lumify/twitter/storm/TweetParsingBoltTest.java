package com.altamiracorp.lumify.twitter.storm;

import static com.altamiracorp.lumify.storm.util.FieldsMatcher.*;
import static org.mockito.Mockito.*;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.twitter.BaseLumifyJsonBolt;
import java.util.Arrays;
import java.util.List;

import com.altamiracorp.securegraph.Vertex;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TweetParsingBolt.class })
public class TweetParsingBoltTest extends BaseTwitterBoltTest<TweetParsingBolt> {
    @Override
    protected TweetParsingBolt createBolt() {
        return new TweetParsingBolt();
    }
    
    @Test
    public void testDeclareOutputFields() {
        OutputFieldsDeclarer ofd = mock(OutputFieldsDeclarer.class);
        instance.declareOutputFields(ofd);
        verify(ofd).declare(argThat(sameFields(BaseLumifyJsonBolt.JSON_FIELD, TwitterStormConstants.TWEET_VERTEX_FIELD)));
    }
    
    @Test
    public void testProcessJson_NoTweet() throws Exception {
        Tuple tuple = mock(Tuple.class);
        JSONObject json = mock(JSONObject.class);

        when(twitterProcessor.parseTweet(anyString(), eq(json))).thenReturn(null);
        
        instance.processJson(json, tuple);
        verify(outputCollector, never()).emit(any(Tuple.class), any(List.class));
    }
    
    @Test
    public void testProcessJson_WithUser() throws Exception {
        Tuple tuple = mock(Tuple.class);
        Vertex tweetVertex = mock(Vertex.class);
        JSONObject json = mock(JSONObject.class);
        String jsonStr = "{\"foo\": \"bar\"}";

        when(json.toString()).thenReturn(jsonStr);
        when(twitterProcessor.parseTweet(anyString(), eq(json))).thenReturn(tweetVertex);
        
        instance.processJson(json, tuple);
        verify(outputCollector).emit(tuple, Arrays.asList(jsonStr, tweetVertex));
    }
}
