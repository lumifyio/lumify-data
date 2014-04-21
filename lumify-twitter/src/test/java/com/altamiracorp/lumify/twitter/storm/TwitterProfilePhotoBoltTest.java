package com.altamiracorp.lumify.twitter.storm;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.securegraph.Vertex;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TwitterUserParsingBolt.class})
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
        verify(twitterProcessor, never()).retrieveProfileImage(anyString(), any(JSONObject.class), any(Vertex.class));
    }

    @Test
    public void testProcessJson_WithUserVertex() throws Exception {
        Tuple tuple = mock(Tuple.class);
        Vertex tweeterVertex = mock(Vertex.class);
        JSONObject json = mock(JSONObject.class);

        when(tuple.getValueByField(TwitterStormConstants.TWITTER_USER_VERTEX_FIELD)).thenReturn(tweeterVertex);

        instance.processJson(json, tuple);
        verify(twitterProcessor).retrieveProfileImage(anyString(), eq(json), eq(tweeterVertex));
    }
}
