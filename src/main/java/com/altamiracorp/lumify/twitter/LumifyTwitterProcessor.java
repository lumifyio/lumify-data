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

package com.altamiracorp.lumify.twitter;

import com.altamiracorp.securegraph.Vertex;
import org.json.JSONObject;

/**
 * The Twitter processor contains the business logic for
 * extracting Tweets, Twitter Users and other entities and
 * relationships from a Twitter JSON feed and storing them
 * in the Lumify repositories.
 */
public interface LumifyTwitterProcessor {
    /**
     * Add the provided Tweet JSONObject to a processing queue.
     * @param queueName the queue name
     * @param tweet the Tweet JSON
     */
    void queueTweet(final String queueName, final JSONObject tweet);
    
    /**
     * Extract a Tweet artifact from the incoming JSON record
     * and stores it in the Lumify repository. This method
     * returns <code>null</code> if the Tweet did not contain
     * the minimum required data fields to be added to the Lumify
     * system.
     * @param processId the name of the process parsing the tweet
     * @param jsonTweet the Tweet JSON
     * @return the Vertex representing the parsed Tweet or <code>null</code> if the Tweet
     * could not be processed
     * @throws Exception if an error occurs during Tweet processing
     */
    Vertex parseTweet(final String processId, final JSONObject jsonTweet) throws Exception;
    
    /**
     * Extract the Twitter User from the incoming JSON record
     * and store it in the Lumify repository.
     * @param processId the name of the process parsing the twitter user
     * @param jsonTweet the Tweet JSON
     * @param tweetVertex the Vertex representing the Tweet
     * @return the Vertex representing the parsed Twitter User
     */
    Vertex parseTwitterUser(final String processId, final JSONObject jsonTweet, final Vertex tweetVertex);
    
    /**
     * Extract the Entities found in the incoming tweet and establish
     * their relationships with the source Tweet.
     * @param processId the name of the process parsing the entities
     * @param jsonTweet the Tweet JSON
     * @param tweetVertex the Vertex representing the Tweet
     * @param entityType the type of Entity to extract
     */
    void extractEntities(final String processId, final JSONObject jsonTweet, final Vertex tweetVertex,
            final TwitterEntityType entityType);
    
    /**
     * Retrieve the profile image for a Twitter User if it
     * is configured and store it in the Lumify repository.
     * @param processId the name of the process retrieving the profile image
     * @param jsonTweet the Tweet JSON
     * @param tweeterVertex the Vertex representing the Twitter User
     */
    void retrieveProfileImage(final String processId, final JSONObject jsonTweet, final Vertex tweeterVertex);
    
    /**
     * Finalize processing of a Tweet artifact, performing any necessary actions for
     * the Vertex once all other processes have completed.
     * @param processId the name of the process finalizing the Tweet
     * @param tweetVertexId the ID of the Tweet vertex
     */
    void finalizeTweetVertex(final String processId, final String tweetVertexId);
}
