package com.altamiracorp.lumify.storm.twitter;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.artifact.ArtifactType;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.graph.GraphGeoLocation;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.beust.jcommander.internal.Lists;
import com.google.inject.Inject;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.altamiracorp.lumify.core.model.ontology.PropertyName.*;
import static com.altamiracorp.lumify.storm.twitter.TwitterConstants.*;

public class TwitterStreamingBolt extends BaseLumifyBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TwitterStreamingBolt.class);
    private static final String PROCESS = TwitterStreamingBolt.class.getName();

    private static final String TWITTER_TEXT_PROPERTY = "text";
    private static final String TWITTER_CREATED_AT_PROPERTY = "created_at";
    private static final String TWITTER_USER_PROPERTY = "user";
    private static final String TWITTER_COORDINATES_PROPERTY = "coordinates";
    private static final String TWITTER_FAVORITE_COUNT_PROPERTY = "favorite_count";
    private static final String TWITTER_RETWEET_COUNT_PROPERTY = "retweet_count";
    private static final String TWITTER_DISPLAY_NAME_PROPERTY = "name";
    private static final String TWITTER_STATUS_COUNT_PROPERTY = "statuses_count";
    private static final String TWITTER_FOLLOWERS_COUNT_PROPERTY = "followers_count";
    private static final String TWITTER_FRIENDS_COUNT_PROPERTY = "friends_count";
    private static final String TWITTER_DESCRIPTION_PROPERTY = "description";
    private static final String TWITTER_SOURCE = "Twitter";
    private static final String TWEET_ARTIFACT_MIME_TYPE = "text/plain";
    private static final String FAVORITE_COUNT = "favoriteCount";
    private static final String RETWEET_COUNT = "retweetCount";
    private static final String STATUS_COUNT = "statusCount";
    private static final String FOLLOWER_COUNT = "followerCount";
    private static final String FOLLOWING_COUNT = "followingCount";
    private static final String CREATION_DATE = "creationDate";
    private static final String DESCRIPTION = "description";
    private static final int LATITUDE_INDEX = 1;
    private static final int LONGITUDE_INDEX = 0;
    private static final int UNSET_INT_VALUE = Integer.MIN_VALUE;

    /**
     * ThreadLocal DateFormat for Twitter dates.  This minimizes creation of
     * non-thread-safe SimpleDateFormat objects and eliminates the need for
     * synchronization of a single DateFormat by creating one format for each
     * Thread.
     */
    private static final ThreadLocal<DateFormat> TWITTER_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat(TWITTER_DATE_FORMAT_STR);
            sdf.setLenient(true);
            return sdf;
        }
    };

    private SearchProvider searchProvider;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(
                TWEET_TEXT_FIELD,
                TWEET_VERTEX_ID_FIELD,
                TWEET_VERTEX_TITLE_FIELD,
                TWITTER_USER_VERTEX_ID_FIELD,
                TWITTER_USER_JSON_FIELD
        ));
    }

    @Override
    public void safeExecute(Tuple tuple) throws Exception {
        final JSONObject json = tryGetJsonFromTuple(tuple);
        //if an actual tweet, process it
        if (isProcessableTweet(json)) {
            TweetTuple outputTuple = new TweetTuple();
            outputTuple.setTweet(json);
            parseTweet(outputTuple);
            parseTweeter(outputTuple);

            // Indexing
            InputStream in = new ByteArrayInputStream(outputTuple.getTweetText().getBytes(TWITTER_CHARSET));
            searchProvider.add(outputTuple.getTweetVertex(), in);

            outputTuple.emit(tuple);
        } else {
            if (json == null) {
                LOGGER.warn("Unable to extract JSON from Tuple.");
            } else {
                LOGGER.warn("JSON Object cannot be processed as Twitter data.");
            }
        }
    }

    private void parseTweet(final TweetTuple outputTuple) {
        User user = getUser();

        JSONObject tweetJson = outputTuple.getTweet();
        String tweetText = outputTuple.getTweetText();
        String createdAtStr = tweetJson.optString(TWITTER_CREATED_AT_PROPERTY, null);
        JSONObject tweeterJson = outputTuple.getUserJSON();
        String tweeter = tweeterJson.getString(SCREEN_NAME_PROPERTY);

        String rowKey = ArtifactRowKey.build(tweetJson.toString().getBytes(TWITTER_CHARSET)).toString();

        ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
        artifactExtractedInfo.setText(tweetText);
        artifactExtractedInfo.setRaw(tweetJson.toString().getBytes(TWITTER_CHARSET));
        artifactExtractedInfo.setMimeType(TWEET_ARTIFACT_MIME_TYPE);
        artifactExtractedInfo.setRowKey(rowKey);
        artifactExtractedInfo.setArtifactType(ArtifactType.DOCUMENT.toString());
        artifactExtractedInfo.setTitle(tweetText);
        artifactExtractedInfo.setAuthor(tweeter);
        artifactExtractedInfo.setSource(TWITTER_SOURCE);
        artifactExtractedInfo.setProcess(PROCESS);

        Date date = parseTwitterDate(createdAtStr);
        if (date != null) {
            artifactExtractedInfo.setDate(date);
        }

        // Write to accumulo and create graph vertex for artifact
        GraphVertex tweetVertex = saveArtifact(artifactExtractedInfo);
        outputTuple.setTweetVertex(tweetVertex);
        String tweetId = tweetVertex.getId();
        LOGGER.info("Saving tweet to accumulo and as graph vertex: %s", tweetId);

        List<String> modifiedProperties = new ArrayList<String>();

        JSONArray coords = tweetJson.optJSONArray(TWITTER_COORDINATES_PROPERTY);
        if (coords != null) {
            Geoshape geo = Geoshape.point(coords.getDouble(LATITUDE_INDEX), coords.getDouble(LONGITUDE_INDEX));
            tweetVertex.setProperty(GEO_LOCATION, geo);
            modifiedProperties.add(GEO_LOCATION.toString());
        }

        Integer favoriteCount = getOptInt(tweetJson, TWITTER_FAVORITE_COUNT_PROPERTY);
        if (favoriteCount != null && favoriteCount > 0) {
            tweetVertex.setProperty(FAVORITE_COUNT, favoriteCount);
            modifiedProperties.add(FAVORITE_COUNT);
        }

        Integer retweetCount = getOptInt(tweetJson, TWITTER_RETWEET_COUNT_PROPERTY);
        if (retweetCount != null && retweetCount > 0) {
            tweetVertex.setProperty(RETWEET_COUNT, retweetCount);
            modifiedProperties.add(RETWEET_COUNT);
        }

        graphRepository.save(tweetVertex, user);
        for (String property : modifiedProperties) {
            auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), tweetVertex, property, PROCESS, "", getUser());
        }
    }

    private void parseTweeter(final TweetTuple outputTuple) {
        User user = getUser();
        Concept handleConcept = ontologyRepository.getConceptByName(TWITTER_HANDLE_CONCEPT, user);

        JSONObject tweeterJson = outputTuple.getUserJSON();
        String tweeter = tweeterJson.getString(SCREEN_NAME_PROPERTY).toLowerCase();
        boolean newVertex = false;
        GraphVertex tweeterVertex = graphRepository.findVertexByExactTitle(tweeter, user);
        if (tweeterVertex == null) {
            newVertex = true;
            tweeterVertex = new InMemoryGraphVertex();
        }

        List<String> modifiedProperties = Lists.newArrayList(
                TITLE.toString(),
                CONCEPT_TYPE.toString()
        );

        tweeterVertex.setProperty(TITLE, tweeter);
        tweeterVertex.setProperty(CONCEPT_TYPE, handleConcept.getId());

        String displayName = tweeterJson.optString(TWITTER_DISPLAY_NAME_PROPERTY, null);
        if (displayName != null && !displayName.trim().isEmpty()) {
            tweeterVertex.setProperty(DISPLAY_NAME, displayName);
            modifiedProperties.add(DISPLAY_NAME.toString());
        }

        JSONArray coords = tweeterJson.optJSONArray(TWITTER_COORDINATES_PROPERTY);
        if (coords != null) {
            tweeterVertex.setProperty(GEO_LOCATION,
                    new GraphGeoLocation(coords.getDouble(LATITUDE_INDEX), coords.getDouble(LONGITUDE_INDEX)));
            modifiedProperties.add(GEO_LOCATION.toString());
        }

        Integer statusCount = getOptInt(tweeterJson, TWITTER_STATUS_COUNT_PROPERTY);
        if (statusCount != null && statusCount > 0) {
            tweeterVertex.setProperty(STATUS_COUNT, statusCount);
            modifiedProperties.add(STATUS_COUNT);
        }

        Integer followersCount = getOptInt(tweeterJson, TWITTER_FOLLOWERS_COUNT_PROPERTY);
        if (followersCount != null && followersCount > 0) {
            tweeterVertex.setProperty(FOLLOWER_COUNT, followersCount);
            modifiedProperties.add(FOLLOWER_COUNT);
        }

        Integer friendsCount = getOptInt(tweeterJson, TWITTER_FRIENDS_COUNT_PROPERTY);
        if (friendsCount != null && friendsCount > 0) {
            tweeterVertex.setProperty(FOLLOWING_COUNT, friendsCount);
            modifiedProperties.add(FOLLOWING_COUNT);
        }

        String createdAtStr = tweeterJson.optString(TWITTER_CREATED_AT_PROPERTY, null);
        Date createdAt = parseTwitterDate(createdAtStr);
        if (createdAt != null) {
            tweeterVertex.setProperty(CREATION_DATE, createdAt.getTime());
            modifiedProperties.add(CREATION_DATE);
        }

        String description = tweeterJson.optString(TWITTER_DESCRIPTION_PROPERTY, null);
        if (description != null && !description.trim().isEmpty()) {
            tweeterVertex.setProperty(DESCRIPTION, description);
            modifiedProperties.add(DESCRIPTION);
        }

        graphRepository.save(tweeterVertex, getUser());
        outputTuple.setUserVertex(tweeterVertex);
        String tweetId = outputTuple.getTweetId();
        String tweeterId = outputTuple.getUserId();

        for (String modifiedProperty : modifiedProperties) {
            auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), tweeterVertex, modifiedProperty, PROCESS, "", getUser());
        }

        graphRepository.saveRelationship(tweeterId, tweetId, TWEETED_RELATIONSHIP, user);
        String relationshipLabelDisplayName = ontologyRepository.getDisplayNameForLabel(TWEETED_RELATIONSHIP, getUser());
        auditRepository.auditRelationships(AuditAction.CREATE.toString(), tweeterVertex, outputTuple.getTweetVertex(), relationshipLabelDisplayName, PROCESS, "", getUser());
    }

    /**
     * Checks the provided object to determine if it contains the minimum
     * required fields to be processed by this bolt.
     *
     * @param json the JSON object
     * @return true if the object can be processed as a tweet
     */
    private boolean isProcessableTweet(final JSONObject json) {
        if (json == null) {
            return false;
        }
        String text = json.optString(TWITTER_TEXT_PROPERTY, null);
        JSONObject user = json.optJSONObject(TWITTER_USER_PROPERTY);
        String scrName = user != null ? user.optString(SCREEN_NAME_PROPERTY, null) : null;
        return text != null &&
                user != null &&
                scrName != null && !scrName.trim().isEmpty();

    }

    private Date parseTwitterDate(String dateStr) {
        Date date = null;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                date = TWITTER_DATE_FORMAT.get().parse(dateStr);
            } catch (ParseException e) {
                throw new RuntimeException(String.format("Unable to parse date string: %s", dateStr), e);
            }
        }
        return date;
    }

    /**
     * Get an optional integer field from the JSONObject as an Integer, returning
     * <code>null</code> if the field is not populated.
     *
     * @param obj      the JSON Object
     * @param property the integer property
     * @return the Integer value of the property or <code>null</code> if not specified
     */
    private Integer getOptInt(final JSONObject obj, final String property) {
        int val = obj.optInt(property, UNSET_INT_VALUE);
        return val != UNSET_INT_VALUE ? new Integer(val) : null;
    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    private class TweetTuple {
        private JSONObject tweet;
        private GraphVertex tweetVertex;
        private GraphVertex userVertex;

        public JSONObject getTweet() {
            return tweet;
        }

        public void setTweet(final JSONObject tweet) {
            this.tweet = tweet;
        }

        public GraphVertex getTweetVertex() {
            return tweetVertex;
        }

        public void setTweetVertex(final GraphVertex tweetVertex) {
            this.tweetVertex = tweetVertex;
        }

        public GraphVertex getUserVertex() {
            return userVertex;
        }

        public void setUserVertex(final GraphVertex userVertex) {
            this.userVertex = userVertex;
        }

        public String getTweetText() {
            return tweet.getString(TWITTER_TEXT_PROPERTY);
        }

        public String getTweetId() {
            return tweetVertex.getId();
        }

        public String getTweetTitle() {
            return tweetVertex.getProperty(TITLE).toString();
        }

        public String getUserId() {
            return userVertex.getId();
        }

        public JSONObject getUserJSON() {
            return tweet.getJSONObject(TWITTER_USER_PROPERTY);
        }

        public void emit(final Tuple parent) {
            getCollector().emit(parent, Arrays.asList(
                    getTweetText(),
                    getTweetId(),
                    getTweetTitle(),
                    getUserId(),
                    getUserJSON()
            ));
        }
    }
}
