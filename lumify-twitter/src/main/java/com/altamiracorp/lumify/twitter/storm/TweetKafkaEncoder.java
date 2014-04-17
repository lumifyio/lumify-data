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

import backtype.storm.tuple.Fields;
import com.altamiracorp.lumify.model.KafkaJsonEncoder;
import com.altamiracorp.lumify.twitter.BaseLumifyJsonBolt;
import com.altamiracorp.lumify.twitter.LumifyTwitterProcessor;
import com.altamiracorp.lumify.twitter.TwitterConstants;
import com.altamiracorp.securegraph.Vertex;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;

/**
 * The TweetKafkaEncoder deserializes a raw Twitter JSON object representing
 a Tweet and processes it to generate a Lumify GraphVertex.  Both the
 * raw JSONObject and the GraphVertex are returned by this Scheme.
 */
public class TweetKafkaEncoder extends KafkaJsonEncoder {
    /**
     * The index of the TweetVertex in the Tuple list.
     */
    public static final int TWEET_VERTEX_IDX = 1;
    
    /**
     * The Twitter Processor.
     */
    private LumifyTwitterProcessor twitterProcessor;

    @Override
    public List<Object> deserialize(byte[] ser) {
        try {
            String jsonStr = new String(ser, TwitterConstants.TWITTER_CHARSET);
            JSONObject tweetJson = new JSONObject(jsonStr);
            Vertex tweetVertex = twitterProcessor.parseTweet(getClass().getName(), tweetJson);
            return Arrays.asList(jsonStr, tweetVertex);
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing tweet from queue.", ex);
        }
    }

    @Override
    public Fields getOutputFields() {
        return new Fields(BaseLumifyJsonBolt.JSON_FIELD, TwitterStormConstants.TWEET_VERTEX_FIELD);
    }
    
    @Inject
    public void setTwitterProcessor(final LumifyTwitterProcessor proc) {
        this.twitterProcessor = proc;
    }
}
