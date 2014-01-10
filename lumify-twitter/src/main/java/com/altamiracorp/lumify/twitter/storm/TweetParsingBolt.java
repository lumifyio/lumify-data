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

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import org.json.JSONObject;

/**
 * This Bolt accepts an input Tuple containing a JSON-serialized Twitter
 * <a href="https://dev.twitter.com/docs/platform-objects/tweets">Tweet</a>
 * object in the <code>json</code> field.  It creates and stores a Lumify
 * artifact for the Tweet and emits a Tuple containing the original JSON and
 * created GraphVertex to downstream Bolts.
 * 
 * <h2>Input Tuple:</h2>
 * <table>
 * <tr><th>Field</th><th>Type</th><th>Value</th></tr>
 * <tr><td>json</td><td>String</td><td>serialized Tweet JSON object</td></tr>
 * </table>
 * 
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
        GraphVertex tweetVertex = getTwitterProcessor().parseTweet(getProcessId(), json);
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
