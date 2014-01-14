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

/**
 * Constants used by the Lumify Twitter Storm topology.
 */
public interface TwitterStormConstants {
    /**
     * The Tweet vertex field.
     */
    static final String TWEET_VERTEX_FIELD = "tweetVertex";
    
    /**
     * The Twitter User (tweeter) vertex field.
     */
    static final String TWITTER_USER_VERTEX_FIELD = "tweeterVertex";
}
