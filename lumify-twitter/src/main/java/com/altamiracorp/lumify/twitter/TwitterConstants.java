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

package com.altamiracorp.lumify.twitter;

import com.altamiracorp.lumify.core.json.IntegerJsonProperty;
import com.altamiracorp.lumify.core.json.JSONObjectJsonProperty;
import com.altamiracorp.lumify.core.json.StringJsonProperty;
import java.nio.charset.Charset;

/**
 * Constants used by the Twitter processing spouts and bolts.
 */
public interface TwitterConstants {
    /**
     * The Tweet Concept name.
     */
    static final String CONCEPT_TWEET = "tweet";
    
    /**
     * The Twitter Handle (user) concept name.
     */
    static final String CONCEPT_TWITTER_HANDLE = "twitterHandle";
    
    /**
     * The Twitter Profile Image concept name.
     */
    static final String CONCEPT_TWITTER_PROFILE_IMAGE = "tweeterProfileImage";
    
    /**
     * The Twitter Mention concept name.
     */
    static final String CONCEPT_TWITTER_MENTION = CONCEPT_TWITTER_HANDLE;
    
    /**
     * The Twitter Hashtag concept name.
     */
    static final String CONCEPT_TWITTER_HASHTAG = "hashtag";
    
    /**
     * The Twitter URL concept name.
     */
    static final String CONCEPT_TWITTER_URL = "url";
    
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
     * The Twitter Character Set.
     */
    static final Charset TWITTER_CHARSET = Charset.forName("UTF-8");
    
    /**
     * The Twitter Queue Name.
     */
    static final String TWITTER_QUEUE_NAME = "twitterStream";
    
    /**
     * The Twitter text property.
     */
    static final StringJsonProperty JSON_TEXT_PROPERTY = new StringJsonProperty("text");
    
    /**
     * The Twitter created at property.
     */
    static final TwitterDateJsonProperty JSON_CREATED_AT_PROPERTY = new TwitterDateJsonProperty("created_at");
    
    /**
     * The Twitter user property.
     */
    static final JSONObjectJsonProperty JSON_USER_PROPERTY = new JSONObjectJsonProperty("user");
    
    /**
     * The Twitter coordinates property.
     */
    static final TwitterGeoPointJsonProperty JSON_COORDINATES_PROPERTY = new TwitterGeoPointJsonProperty("coordinates");
    
    /**
     * The Twitter favorite count property.
     */
    static final IntegerJsonProperty JSON_FAVORITE_COUNT_PROPERTY = new IntegerJsonProperty("favorite_count");
    
    /**
     * The Twitter retweet count property.
     */
    static final IntegerJsonProperty JSON_RETWEET_COUNT_PROPERTY = new IntegerJsonProperty("retweet_count");
    
    /**
     * The Twitter user screen name property.
     */
    static final StringJsonProperty JSON_SCREEN_NAME_PROPERTY = new StringJsonProperty("screen_name");
    
    /**
     * The Twitter user display name property.
     */
    static final StringJsonProperty JSON_DISPLAY_NAME_PROPERTY = new StringJsonProperty("name");
    
    /**
     * The Twitter status count property.
     */
    static final IntegerJsonProperty JSON_STATUS_COUNT_PROPERTY = new IntegerJsonProperty("statuses_count");
    
    /**
     * The Twitter followers count property.
     */
    static final IntegerJsonProperty JSON_FOLLOWERS_COUNT_PROPERTY = new IntegerJsonProperty("followers_count");
    
    /**
     * The Twitter friends count property.
     */
    static final IntegerJsonProperty JSON_FRIENDS_COUNT_PROPERTY = new IntegerJsonProperty("friends_count");
    
    /**
     * The Twitter user description property.
     */
    static final StringJsonProperty JSON_DESCRIPTION_PROPERTY = new StringJsonProperty("description");
    
    /**
     * The Twitter profile image URL property.
     */
    static final StringJsonProperty JSON_PROFILE_IMAGE_URL_PROPERTY = new StringJsonProperty("profile_image_url");
    
    /**
     * The Lumify favorite count property.
     */
    static final String LUMIFY_FAVORITE_COUNT_PROPERTY = "favoriteCount";
    
    /**
     * The Lumify retweet count property.
     */
    static final String LUMIFY_RETWEET_COUNT_PROPERTY = "retweetCount";
    
    /**
     * The Lumify status count property.
     */
    static final String LUMIFY_STATUS_COUNT_PROPERTY = "statusCount";
    
    /**
     * The Lumify follower count property.
     */
    static final String LUMIFY_FOLLOWER_COUNT_PROPERTY = "followerCount";
    
    /**
     * The Lumify following count property.
     */
    static final String LUMIFY_FOLLOWING_COUNT_PROPERTY = "followingCount";
    
    /**
     * The Lumify creation date property.
     */
    static final String LUMIFY_CREATION_DATE_PROPERTY = "creationDate";
    
    /**
     * The Lumify description property.
     */
    static final String LUMIFY_DESCRIPTION_PROPERTY = "description";
}
