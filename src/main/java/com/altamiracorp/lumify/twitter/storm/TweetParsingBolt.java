package com.altamiracorp.lumify.twitter.storm;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.securegraph.Vertex;
import org.json.JSONObject;

/**
 * This Bolt accepts an input Tuple containing a JSON-serialized Twitter
 * <a href="https://dev.twitter.com/docs/platform-objects/tweets">Tweet</a>
 * object in the <code>json</code> field.  It creates and stores a Lumify
 * artifact for the Tweet and emits a Tuple containing the original JSON and
 * created GraphVertex to downstream Bolts.
 * <p/>
 * <h2>Input Tuple:</h2>
 * <table>
 * <tr><th>Field</th><th>Type</th><th>Value</th></tr>
 * <tr><td>json</td><td>String</td><td>serialized Tweet JSON object</td></tr>
 * </table>
 * <p/>
 * <h2>Output Tuple:</h2>
 * <table>
 * <tr><th>Field</th><th>Type</th><th>Value</th></tr>
 * <tr><td>json</td><td>String</td><td>serialized Tweet JSON object</td></tr>
 * <tr><td>tweetVertex</td><td>GraphVertex</td><td>the Lumify GraphVertex for the Tweet artifact</td></tr>
 * </table>
 */
public class TweetParsingBolt extends BaseTwitterBolt {
    @Override
    protected void processJson(final JSONObject json, final Tuple input) throws Exception {
        // extract the Tweet from the JSON object and create the Lumify artifacts
        Vertex tweetVertex = getTwitterProcessor().parseTweet(getProcessId(), json);
        // if the Tweet was successfully parsed, emit the JSON object and tweetVertex
        // to downstream bolts
        if (tweetVertex != null) {
            emit(input, json.toString(), tweetVertex);
        }
    }

    @Override
    public void declareOutputFields(final OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(
                JSON_FIELD,
                TwitterStormConstants.TWEET_VERTEX_FIELD
        ));
    }
}
