package com.altamiracorp.lumify.twitter.storm;

import static org.mockito.Mockito.*;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TweetQueueOutputBoltTest extends BaseTwitterBoltTest<TweetQueueOutputBolt> {
    private static final String TEST_QUEUE_NAME = "testQueue";
    
    @Override
    protected TweetQueueOutputBolt createBolt() {
        return new TweetQueueOutputBolt(TEST_QUEUE_NAME);
    }
    
    @Test
    public void testDeclareOutputFields() {
        OutputFieldsDeclarer ofd = mock(OutputFieldsDeclarer.class);
        instance.declareOutputFields(ofd);
        verify(ofd, never()).declare(any(Fields.class));
    }
    
    @Test
    public void testProcessJson_WithTweetVertex() throws Exception {
        Tuple tuple = mock(Tuple.class);
        JSONObject json = mock(JSONObject.class);

        instance.processJson(json, tuple);
        verify(twitterProcessor).queueTweet(TEST_QUEUE_NAME, json);
    }
}
