/*
 * Copyright 2013 Altamira Corporation.
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

package com.altamiracorp.lumify.storm.twitter;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;

import java.util.Arrays;

import static com.altamiracorp.lumify.storm.twitter.TwitterConstants.*;

/**
 * Base class for bolts that fork processing for joining in a later bolt.
 */
public abstract class BaseTwitterForkBolt extends BaseLumifyBolt {
    /**
     * The bolt ID, used to identify when joins can occur.
     */
    private final String boltId;

    /**
     * Create a new BaseTwitterForkBolt.
     *
     * @param id the bolt ID
     */
    protected BaseTwitterForkBolt(final String id) {
        boltId = id;
    }

    /**
     * Get the bolt ID.
     *
     * @return the bolt ID
     */
    protected final String getBoltId() {
        return boltId;
    }

    @Override
    public void declareOutputFields(final OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(TWEET_VERTEX_ID_FIELD, TWITTER_BOLT_ID_FIELD));
    }

    @Override
    protected final void safeExecute(final Tuple input) throws Exception {
        try {
            executeFork(input);
        } finally {
            getCollector().emit(input, Arrays.asList((Object) input.getStringByField(TWEET_VERTEX_ID_FIELD), getBoltId()));
        }
    }

    /**
     * Execute the logic of the forked bolt.
     *
     * @param input the Tuple to execute on
     * @throws Exception if an error occurs during execution
     */
    protected abstract void executeFork(final Tuple input) throws Exception;
}
