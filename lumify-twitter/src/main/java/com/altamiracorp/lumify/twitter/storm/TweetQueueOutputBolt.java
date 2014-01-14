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
import backtype.storm.tuple.Tuple;
import org.json.JSONObject;

/**
 * This bolt accepts a Tweet JSONObject in its input Tuple and writes
 * the object to a configured queue.  It ignores both missing and invalid
 * JSON in the input Tuple.
 */
public class TweetQueueOutputBolt extends BaseTwitterBolt {
    /**
     * The queue name.
     */
    private final String queueName;

    /**
     * Create a new TweetQueueOutputBolt.
     * @param queue the queue to output to
     */
    public TweetQueueOutputBolt(final String queue) {
        super(JsonHandlingPolicy.IGNORE, JsonHandlingPolicy.IGNORE);
        this.queueName = queue;
    }

    @Override
    protected void processJson(final JSONObject json, final Tuple input) throws Exception {
        getTwitterProcessor().queueTweet(queueName, json);
    }

    @Override
    public void declareOutputFields(final OutputFieldsDeclarer ofd) {
    }
}
