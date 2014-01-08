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
import com.altamiracorp.lumify.storm.BaseLumifyJsonBolt;
import com.altamiracorp.lumify.twitter.LumifyTwitterProcessor;
import com.google.inject.Inject;
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
        List<Object> values = super.deserialize(ser);
        if (values.get(0) instanceof JSONObject) {
            JSONObject tweetJson = (JSONObject) values.get(0);
            try {
                values.add(twitterProcessor.parseTweet(EXTRA, tweetJson));
            } catch (Exception e) {
                throw new RuntimeException("Unable to parse tweet.", e);
            }
        }
        return values;
    }

    @Override
    public Fields getOutputFields() {
        return new Fields(BaseLumifyJsonBolt.JSON_FIELD, TwitterStormConstants.TWEET_VERTEX_FIELD);
    }
    
    @Inject
    public void setTwitterProcessor(final LumifyTwitterProcessor proc) {
        twitterProcessor = proc;
    }
}
