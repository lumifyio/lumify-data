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

import java.util.Collection;

/**
 * This bolt submits Tweet artifacts for highlighting once
 * all processing on the Tweet is complete.
 */
public class TwitterTweetHighlightSubmitBolt extends BaseTwitterJoinBolt {
    /**
     * Create a new TwitterTweetHighlightSubmitBolt.
     *
     * @param joinIds the IDs of the bolts to join on
     */
    public TwitterTweetHighlightSubmitBolt(final Collection<String> joinIds) {
        super(joinIds);
    }

    @Override
    protected void executeJoin(final String tweetVertexId) throws Exception {
        workQueueRepository.pushArtifactHighlight(tweetVertexId);
    }
}
