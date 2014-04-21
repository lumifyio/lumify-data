package com.altamiracorp.lumify.twitter.storm;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.securegraph.Vertex;
import org.json.JSONObject;

/**
 * This Bolt accepts an input Tuple containing a JSON-serialized Twitter
 * <a href="https://dev.twitter.com/docs/platform-objects/tweets">Tweet</a>
 * object in the <code>json</code> field and the Lumify GraphVertex representing
 * the Twitter user entity in the <code>tweeterVertex</code> field.  It retrieves
 * the profile photo from the URL, if any, specified in the Tweet JSON and stores
 * it in Lumify, linking it to the Twitter User entity.
 * 
 * <h2>Input Tuple:</h2>
 * <table>
 * <tr><th>Field</th><th>Type</th><th>Value</th></tr>
 * <tr><td>json</td><td>String</td><td>serialized Tweet JSON object</td></tr>
 * <tr><td>tweeterVertex</td><td>GraphVertex</td><td>the Lumify GraphVertex for the Twitter user entity</td></tr>
 * </table>
 */
public class TwitterProfilePhotoBolt extends BaseTwitterBolt {
    @Override
    protected void processJson(final JSONObject json, final Tuple input) throws Exception {
        Vertex tweeterVertex = (Vertex) input.getValueByField(TwitterStormConstants.TWITTER_USER_VERTEX_FIELD);
        if (tweeterVertex != null) {
            getTwitterProcessor().retrieveProfileImage(getProcessId(), json, tweeterVertex);
        }
    }

    @Override
    public void declareOutputFields(final OutputFieldsDeclarer declarer) {
    }
}
