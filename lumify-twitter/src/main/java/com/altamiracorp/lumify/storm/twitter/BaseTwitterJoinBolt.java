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

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;

import java.util.*;

import static com.altamiracorp.lumify.storm.twitter.TwitterConstants.TWEET_VERTEX_ID_FIELD;
import static com.altamiracorp.lumify.storm.twitter.TwitterConstants.TWITTER_BOLT_ID_FIELD;

/**
 * Base class for bolts that join one or more forked bolt processes.
 */
public abstract class BaseTwitterJoinBolt extends BaseLumifyBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(BaseTwitterJoinBolt.class);

    /**
     * The bolt IDs to join on.
     */
    private final Set<String> joinIds;

    /**
     * The map of vertex IDs to forked tuples.
     */
    private final Map<String, Set<String>> joinMap;

    /**
     * Create a new BaseTwitterJoinBolt.
     *
     * @param ids the bolt IDs to join on
     */
    protected BaseTwitterJoinBolt(final Collection<String> ids) {
        joinIds = Collections.unmodifiableSet(new HashSet<String>(ids));
        joinMap = Collections.synchronizedMap(new HashMap<String, Set<String>>());
    }

    @Override
    protected void safeExecute(final Tuple input) throws Exception {
        String vertexId = input.getStringByField(TWEET_VERTEX_ID_FIELD);
        String boltId = input.getStringByField(TWITTER_BOLT_ID_FIELD);
        boolean execJoin = false;
        synchronized (joinMap) {
            Set<String> received = joinMap.get(vertexId);
            if (received == null) {
                received = new HashSet<String>();
                joinMap.put(vertexId, received);
            }
            received.add(boltId);
            if (joinIds.equals(received)) {
                execJoin = true;
                joinMap.remove(vertexId);
            }
        }
        if (execJoin) {
            LOGGER.info("Executing join for Vertex: %s", vertexId);
            executeJoin(vertexId);
        }
    }

    /**
     * Execute the join code once tuples from all fork bolts have been received.
     *
     * @param tweetVertexId the vertex ID of the forked tweet
     * @throws Exception if an error occurs
     */
    protected abstract void executeJoin(final String tweetVertexId) throws Exception;
}
