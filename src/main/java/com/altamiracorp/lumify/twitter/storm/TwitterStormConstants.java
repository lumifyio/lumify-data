package com.altamiracorp.lumify.twitter.storm;

/**
 * Constants used by the Lumify Twitter Storm topology.
 */
public interface TwitterStormConstants {
    /**
     * The Tweet vertex field.
     */
    String TWEET_VERTEX_FIELD = "tweetVertex";
    
    /**
     * The Twitter User (tweeter) vertex field.
     */
    String TWITTER_USER_VERTEX_FIELD = "tweeterVertex";
}
