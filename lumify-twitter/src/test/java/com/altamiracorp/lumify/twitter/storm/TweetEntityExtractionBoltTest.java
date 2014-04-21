package com.altamiracorp.lumify.twitter.storm;

import static org.mockito.Mockito.*;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.twitter.TwitterEntityType;
import com.altamiracorp.securegraph.Vertex;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TweetParsingBolt.class })
public class TweetEntityExtractionBoltTest extends BaseTwitterBoltTest<TweetEntityExtractionBolt> {
    /**
     * The test entity type.
     */
    private static final TwitterEntityType TEST_ENTITY_TYPE = TwitterEntityType.MENTION;
    
    @Override
    protected TweetEntityExtractionBolt createBolt() {
        return new TweetEntityExtractionBolt(TEST_ENTITY_TYPE);
    }
    
    @Test
    public void testDeclareOutputFields() {
        OutputFieldsDeclarer ofd = mock(OutputFieldsDeclarer.class);
        instance.declareOutputFields(ofd);
        verify(ofd, never()).declare(any(Fields.class));
    }
    
    @Test
    public void testProcessJson() throws Exception {
        Tuple tuple = mock(Tuple.class);
        Vertex tweetVertex = mock(Vertex.class);
        JSONObject json = mock(JSONObject.class);
        
        when(tuple.getValueByField(TwitterStormConstants.TWEET_VERTEX_FIELD)).thenReturn(tweetVertex);
        
        instance.processJson(json, tuple);
        verify(twitterProcessor).extractEntities(anyString(), eq(json), eq(tweetVertex), eq(TEST_ENTITY_TYPE));
    }
}
