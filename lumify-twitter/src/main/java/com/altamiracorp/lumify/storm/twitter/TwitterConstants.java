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

import java.nio.charset.Charset;

/**
 * Constants used by the Twitter processing spouts and bolts.
 */
public interface TwitterConstants {
    /**
     * The name of the Tuple field containing the tweet text.
     */
    static final String TWEET_TEXT_FIELD = "tweet-text";
    
    /**
     * The name of the Tuple field containing the Vertex ID of the
     * processed Tweet.
     */
    static final String TWEET_VERTEX_ID_FIELD = "tweet-vertex-id";
    
    /**
     * The name of the Tuple field containing the title property of
     * the Tweet vertex.
     */
    static final String TWEET_VERTEX_TITLE_FIELD = "tweet-vertex-title";
    
    /**
     * The name of the Tuple field containing the Vertex ID of the processed
     * Twitter user.
     */
    static final String TWITTER_USER_VERTEX_ID_FIELD = "tweeter-vertex-id";
    
    /**
     * The name of the Tuple field containing the Twitter user JSONObject.
     */
    static final String TWITTER_USER_JSON_FIELD = "tweeter-json";
    
    /**
     * The name of the Tuple field containing the bolt ID.
     */
    static final String TWITTER_BOLT_ID_FIELD = "bolt-id";
    
    /**
     * The Twitter Date format string.
     */
    static final String TWITTER_DATE_FORMAT_STR = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

    /**
     * The Twitter tweet concept name.
     */
    static final String TWITTER_CONCEPT = "tweet";

    /**
     * The Twitter tweeter profile phot image.
     */
    static final String TWEETER_PROFILE_IMAGE_CONCEPT = "tweeterProfileImage";
    
    /**
     * The Twitter Handle (user) concept name.
     */
    static final String TWITTER_HANDLE_CONCEPT = "twitterHandle";
    
    /**
     * The Twitter Mention concept name.
     */
    static final String TWITTER_MENTION_CONCEPT = TWITTER_HANDLE_CONCEPT;
    
    /**
     * The Twitter Hashtag concept name.
     */
    static final String TWITTER_HASHTAG_CONCEPT = "hashtag";
    
    /**
     * The Twitter URL concept name.
     */
    static final String TWITTER_URL_CONCEPT = "url";
    
    /**
     * The Tweeted relationship label.
     */
    static final String TWEETED_RELATIONSHIP = "twitterHandleTweetedTweet";
    
    /**
     * The Tweet Mention relationship label.
     */
    static final String TWEET_MENTION_RELATIONSHIP = "tweetMentionedHandle";
    
    /**
     * The Tweet Hashtag relationship label.
     */
    static final String TWEET_HASHTAG_RELATIONSHIP = "tweetHasHashtag";
    
    /**
     * The Tweet URL relationship label.
     */
    static final String TWEET_URL_RELATIONSHIP = "tweetHasURL";
    
    /**
     * The Twitter user screen name property.
     */
    static final String SCREEN_NAME_PROPERTY = "screen_name";
    
    /**
     * The Twitter Character Set.
     */
    static final Charset TWITTER_CHARSET = Charset.forName("UTF-8");
}
