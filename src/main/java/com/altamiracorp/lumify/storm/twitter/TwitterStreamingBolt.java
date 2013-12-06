package com.altamiracorp.lumify.storm.twitter;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRegexFinder;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifact.ArtifactMetadata;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.artifact.ArtifactType;
import com.altamiracorp.lumify.core.model.graph.GraphGeoLocation;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.LabelName;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.model.termMention.TermMention;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.google.inject.Inject;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Integer;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class TwitterStreamingBolt extends BaseLumifyBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterStreamingBolt.class);
    private static final String TWITTER_HANDLE = "twitterHandle";
    private static final String TWEETED = "twitterHandleTweetedTweet";
    private static final String TWEET_MENTION = "tweetMentionedHandle";
    private static final String TWEET_HASHTAG = "tweetHasHashtag";
    private static final String TWEET_URL = "tweetHasURL";
    private static final String HASHTAG_CONCEPT = "hashtag";
    private static final String URL_CONCEPT = "url";
    private static final String FAVORITE_COUNT = "favoriteCount";
    private static final String RETWEET_COUNT = "retweetCount";
    private static final String STATUS_COUNT = "statusCount";
    private static final String FOLLOWER_COUNT = "followerCount";
    private static final String FOLLOWING_COUNT = "followingCount";

    private SearchProvider searchProvider;
    private GraphVertex tweet;
    private String text;
    Concept handleConcept;

    @Override
    public void safeExecute(Tuple tuple) throws Exception {
        final JSONObject json = getJsonFromTuple(tuple);

        //if an actual tweet, process it
        if (json.has("text")) {
            handleConcept = ontologyRepository.getConceptByName(TWITTER_HANDLE, getUser());
            saveToDatabase(json, handleConcept);

            // Indexing
            InputStream in = new ByteArrayInputStream(text.getBytes());
            searchProvider.add(tweet, in);

            // Creating entities for mentions, hashtags, and urls
            createMentionEntities(handleConcept);
            createHashTagEntities();
            createURLEntities();

            workQueueRepository.pushArtifactHighlight(tweet.getId());
        }

        getCollector().ack(tuple);
    }

    private void saveToDatabase(JSONObject json, Concept handleConcept) {
        text = json.getString("text");
        String createdAt = json.has("created_at") ? json.getString("created_at") : null;
        String tweeter = json.getJSONObject("user").getString("screen_name");
        String source = "Twitter";

        ArtifactRowKey build = ArtifactRowKey.build(json.toString().getBytes());
        String rowKey = build.toString();

        ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
        artifactExtractedInfo.setText(text);
        artifactExtractedInfo.setRaw(json.toString().getBytes());

        artifactExtractedInfo.setMimeType("text/plain");
        artifactExtractedInfo.setRowKey(rowKey);
        artifactExtractedInfo.setArtifactType(ArtifactType.DOCUMENT.toString());
        artifactExtractedInfo.setTitle(text);

        // Write to accumulo and create graph vertex for artifact
        tweet = saveArtifact(artifactExtractedInfo);
        String tweetId = tweet.getId();
        auditRepository.audit(tweetId, auditRepository.createEntityAuditMessage(), getUser());

        LOGGER.info("Saving tweet to accumulo and as graph vertex: " + tweet.getId());

        if (createdAt != null) {
            final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
            SimpleDateFormat sf = new SimpleDateFormat(TWITTER);
            sf.setLenient(true);

            Date date = null;
            try {
                date = sf.parse(createdAt);
            } catch (ParseException e) {
                new RuntimeException("Cannot parse " + createdAt);
            }
            artifactExtractedInfo.setDate(date);

            auditRepository.audit(tweetId, auditRepository.vertexPropertyAuditMessage(tweet, PropertyName.PUBLISHED_DATE.toString(), date.getTime()), getUser());
            tweet.setProperty(PropertyName.PUBLISHED_DATE, date.getTime());
        } else {
            Long time = new Date().getTime();
            auditRepository.audit(tweetId, auditRepository.vertexPropertyAuditMessage(tweet, PropertyName.PUBLISHED_DATE.toString(), time), getUser());
            tweet.setProperty(PropertyName.PUBLISHED_DATE, new Date().getTime());
        }

        if (json.has("coordinates") && !json.get("coordinates").equals(JSONObject.NULL)) {
            JSONArray coordinates = json.getJSONObject("coordinates").getJSONArray("coordinates");
            Geoshape geo = Geoshape.point(coordinates.getDouble(1), coordinates.getDouble(0));
            auditRepository.audit(tweetId, auditRepository.vertexPropertyAuditMessage(tweet, PropertyName.GEO_LOCATION.toString(), coordinates.toString()), getUser());
            tweet.setProperty(PropertyName.GEO_LOCATION, geo);
            artifactExtractedInfo.set(PropertyName.GEO_LOCATION.toString(), geo);
        }

        auditRepository.audit(tweetId, auditRepository.vertexPropertyAuditMessage(tweet, PropertyName.AUTHOR.toString(), "@" + tweeter), getUser());
        tweet.setProperty(PropertyName.AUTHOR, tweeter);

        auditRepository.audit(tweetId, auditRepository.vertexPropertyAuditMessage(tweet, PropertyName.TITLE.toString(), text), getUser());
        tweet.setProperty(PropertyName.TITLE, text);

        auditRepository.audit(tweetId, auditRepository.vertexPropertyAuditMessage(tweet, PropertyName.ROW_KEY.toString(), rowKey), getUser());
        tweet.setProperty(PropertyName.ROW_KEY, rowKey);

        auditRepository.audit(tweetId, auditRepository.vertexPropertyAuditMessage(tweet, PropertyName.SOURCE.toString(), source), getUser());
        tweet.setProperty(PropertyName.SOURCE, source);

        if (json.has("favorite_count") && ((Integer) json.get("favorite_count") > 0)) {
            auditRepository.audit(tweetId, auditRepository.vertexPropertyAuditMessage(tweet, FAVORITE_COUNT, json.get("favorite_count")), getUser());
            tweet.setProperty(FAVORITE_COUNT, json.get("favorite_count"));
        }

        if (json.has("retweet_count") && ((Integer) json.get("retweet_count") > 0)) {
            auditRepository.audit(tweetId, auditRepository.vertexPropertyAuditMessage(tweet,RETWEET_COUNT, json.get("retweet_count")), getUser());
            tweet.setProperty(RETWEET_COUNT, json.get("retweet_count"));
        }

        graphRepository.save(tweet, getUser());

        String tweeterId = createOrUpdateTweeterEntity(handleConcept, (JSONObject) json.get("user"));
        graphRepository.saveRelationship(tweeterId, tweet.getId(), TWEETED, getUser());
    }

    private String createOrUpdateTweeterEntity(Concept handleConcept, JSONObject user) {
        String tweeter = user.getString("screen_name").toLowerCase();
        boolean newVertex = false;
        GraphVertex tweeterVertex = graphRepository.findVertexByTitleAndType(tweeter, VertexType.ENTITY, getUser());
        if (tweeterVertex == null) {
            newVertex = true;
            tweeterVertex = new InMemoryGraphVertex();
        }

        if (!newVertex) {
            String tweeterId = tweeterVertex.getId();
            auditRepository.audit(tweeterId, auditRepository.vertexPropertyAuditMessage(tweeterVertex, PropertyName.TITLE.toString(), "@" + tweeter), getUser());
            auditRepository.audit(tweeterId, auditRepository.vertexPropertyAuditMessage(tweeterVertex, PropertyName.TYPE.toString(), VertexType.ENTITY.toString()), getUser());
            auditRepository.audit(tweeterId, auditRepository.vertexPropertyAuditMessage(tweeterVertex, PropertyName.SUBTYPE.toString(), handleConcept.getId()), getUser());
            auditRepository.audit(tweeterId, auditRepository.vertexPropertyAuditMessage(tweeterVertex, PropertyName.DISPLAY_NAME.toString(), user.getString("name")), getUser());
        }
        tweeterVertex.setProperty(PropertyName.TITLE, tweeter);
        tweeterVertex.setProperty(PropertyName.TYPE, VertexType.ENTITY.toString());
        tweeterVertex.setProperty(PropertyName.SUBTYPE, handleConcept.getId());
        tweeterVertex.setProperty(PropertyName.DISPLAY_NAME, user.getString("name"));
        graphRepository.save(tweeterVertex, getUser());

        addHandleProperties(user, tweeterVertex);

        if (newVertex) {
            auditRepository.audit(tweeterVertex.getId(), auditRepository.vertexPropertyAuditMessage(PropertyName.TITLE.toString(), "@" + tweeter), getUser());
            auditRepository.audit(tweeterVertex.getId(), auditRepository.vertexPropertyAuditMessage(PropertyName.TYPE.toString(), VertexType.ENTITY.toString()), getUser());
            auditRepository.audit(tweeterVertex.getId(), auditRepository.vertexPropertyAuditMessage(PropertyName.SUBTYPE.toString(), handleConcept.getId()), getUser());
            auditRepository.audit(tweeterVertex.getId(), auditRepository.vertexPropertyAuditMessage(PropertyName.DISPLAY_NAME.toString(), user.getString("name")), getUser());
        }


        createProfilePhotoArtifact(user, tweeterVertex);

        return tweeterVertex.getId();
    }

    private void createMentionEntities(Concept handleConcept) {
        createEntities(handleConcept, "(@(\\w+))", TWEET_MENTION);
    }

    private void createHashTagEntities() {
        Concept hashtagConcept = ontologyRepository.getConceptByName(HASHTAG_CONCEPT, getUser());
        createEntities(hashtagConcept, "(#(\\w+))", TWEET_HASHTAG);
    }

    private void createURLEntities() {
        Concept urlConcept = ontologyRepository.getConceptByName(URL_CONCEPT, getUser());
        createEntities(urlConcept, "((http://[^\\s]+))", TWEET_URL);
    }

    private void createEntities(Concept concept, String regex, String relationshipLabel) {
        auditRepository.audit(tweet.getId(), auditRepository.createEntityAuditMessage(), getUser());
        GraphVertex conceptVertex = graphRepository.findVertex(concept.getId(), getUser());
        List<TermMention> termMentionList = TermRegexFinder.find(tweet.getId(), conceptVertex, text, regex);
        for (TermMention mention : termMentionList) {
            String sign = mention.getMetadata().getSign().toLowerCase();
            String rowKey = mention.getRowKey().toString();
            String conceptId = concept.getId();

            GraphVertex vertex = graphRepository.findVertexByTitleAndType(sign, VertexType.ENTITY, getUser());

            boolean newVertex = false;
            if (vertex == null) {
                newVertex = true;
                vertex = new InMemoryGraphVertex();
            }

            if (!newVertex) {
                String vertexId = vertex.getId();
                auditRepository.audit(vertexId, auditRepository.vertexPropertyAuditMessage(vertex, PropertyName.TITLE.toString(), sign), getUser());
                auditRepository.audit(vertexId, auditRepository.vertexPropertyAuditMessage(vertex, PropertyName.ROW_KEY.toString(), rowKey), getUser());
                auditRepository.audit(vertexId, auditRepository.vertexPropertyAuditMessage(vertex, PropertyName.TYPE.toString(), VertexType.ENTITY.toString()), getUser());
                auditRepository.audit(vertexId, auditRepository.vertexPropertyAuditMessage(vertex, PropertyName.SUBTYPE.toString(), conceptId), getUser());
            }
            vertex.setProperty(PropertyName.TITLE, sign);
            vertex.setProperty(PropertyName.ROW_KEY, rowKey);
            vertex.setProperty(PropertyName.TYPE, VertexType.ENTITY.toString());
            vertex.setProperty(PropertyName.SUBTYPE, conceptId);
            graphRepository.save(vertex, getUser());

            if (newVertex) {
                auditRepository.audit(vertex.getId(), auditRepository.vertexPropertyAuditMessage(PropertyName.TITLE.toString(), sign), getUser());
                auditRepository.audit(vertex.getId(), auditRepository.vertexPropertyAuditMessage(PropertyName.ROW_KEY.toString(), rowKey), getUser());
                auditRepository.audit(vertex.getId(), auditRepository.vertexPropertyAuditMessage(PropertyName.TYPE.toString(), VertexType.ENTITY.toString()), getUser());
                auditRepository.audit(vertex.getId(), auditRepository.vertexPropertyAuditMessage(PropertyName.SUBTYPE.toString(), conceptId), getUser());
            }

            mention.getMetadata().setGraphVertexId(vertex.getId());
            termMentionRepository.save(mention, getUser().getModelUserContext());

            graphRepository.saveRelationship(tweet.getId(), vertex.getId(), relationshipLabel, getUser());
        }

    }

    public void addHandleProperties(JSONObject user, GraphVertex handleVertex) {
        handleVertex.setProperty(PropertyName.DISPLAY_NAME, user.getString("name"));
        if (user.has("coordinates") && !user.get("coordinates").equals(JSONObject.NULL)) {
            JSONArray coordinates = user.getJSONObject("coordinates").getJSONArray("coordinates");
            handleVertex.setProperty(PropertyName.GEO_LOCATION, new GraphGeoLocation(coordinates.getDouble(1), coordinates.getDouble(0)));
            auditRepository.audit(handleVertex.getId(), auditRepository.vertexPropertyAuditMessage(tweet, PropertyName.GEO_LOCATION.toString(), coordinates.toString()), getUser());
        }

        if (user.has("statuses_count") && ((Integer) user.get("statuses_count") > 0)) {
            String tweetCount = user.get("statuses_count").toString();
            auditRepository.audit(handleVertex.getId(), auditRepository.vertexPropertyAuditMessage(handleVertex, STATUS_COUNT, tweetCount), getUser());
            handleVertex.setProperty(STATUS_COUNT, tweetCount);
        }

        if (user.has("followers_count") && ((Integer) user.get("followers_count") > 0)) {
            String followersCount = user.get("followers_count").toString();
            auditRepository.audit(handleVertex.getId(), auditRepository.vertexPropertyAuditMessage(handleVertex, FOLLOWER_COUNT, followersCount), getUser());
            handleVertex.setProperty(FOLLOWER_COUNT, followersCount);
        }

        if (user.has("friends_count") && ((Integer) user.get("friends_count") > 0)) {
            String friendsCount = user.get("friends_count").toString();
            auditRepository.audit(handleVertex.getId(), auditRepository.vertexPropertyAuditMessage(handleVertex, FOLLOWING_COUNT, friendsCount), getUser());
            handleVertex.setProperty(FOLLOWING_COUNT, friendsCount);
        }

        String createdAt = user.has("created_at") ? user.getString("created_at") : null;
        if (createdAt != null) {
            final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
            SimpleDateFormat sf = new SimpleDateFormat(TWITTER);
            sf.setLenient(true);

            Date date = null;
            try {
                date = sf.parse(createdAt);
            } catch (ParseException e) {
                new RuntimeException("Cannot parse " + createdAt);
            }
            auditRepository.audit(handleVertex.getId(), auditRepository.vertexPropertyAuditMessage(handleVertex, "creationDate", date.getTime()), getUser());
            handleVertex.setProperty("creationDate", date.getTime());
        }

        if (user.has("description") && !user.get("description").equals(JSONObject.NULL)) {
            auditRepository.audit(handleVertex.getId(), auditRepository.vertexPropertyAuditMessage(handleVertex, "description", user.getString("description")), getUser());
            handleVertex.setProperty("description", user.getString("description"));
        }
        graphRepository.save(handleVertex, getUser());
    }

    public void createProfilePhotoArtifact(JSONObject user, GraphVertex userVertex) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            URL url = new URL(user.get("profile_image_url").toString());
            InputStream is = url.openStream();
            IOUtils.copy(is, os);
            byte[] raw = os.toByteArray();
            ArtifactRowKey build = ArtifactRowKey.build(raw);
            String rowKey = build.toString();
            String fileName = user.getString("screen_name") + "ProfilePicture";
            Artifact artifact = new Artifact(rowKey);

            ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
            artifactExtractedInfo.setMimeType("image/png");
            artifactExtractedInfo.setRowKey(rowKey);
            artifactExtractedInfo.setArtifactType(ArtifactType.IMAGE.toString());
            artifactExtractedInfo.setTitle(user.getString("screen_name") + " Twitter Profile Picture");
            artifactExtractedInfo.setSource("Twitter profile picture");
            artifactExtractedInfo.setRaw(raw);

            ArtifactMetadata metadata = artifact.getMetadata();
            metadata.setCreateDate(new Date());
            metadata.setRaw(raw);
            metadata.setFileName(fileName);
            metadata.setFileExtension(FilenameUtils.getExtension(fileName));
            metadata.setMimeType("image/png");

            artifactRepository.save(artifact, getUser().getModelUserContext());
            GraphVertex profile = saveArtifact(artifactExtractedInfo);

            auditRepository.audit(userVertex.getId(), auditRepository.vertexPropertyAuditMessage(userVertex, PropertyName.GLYPH_ICON.toString(), profile.getId()), getUser());
            userVertex.setProperty(PropertyName.GLYPH_ICON.toString(), "/artifact/" + rowKey + "/raw");
            graphRepository.save(userVertex, getUser());

            graphRepository.findOrAddRelationship(userVertex.getId(), profile.getId(), LabelName.HAS_IMAGE, getUser());

            LOGGER.info("Saving tweeter profile picture to accumulo and as graph vertex: " + profile.getId());
        } catch (IOException e) {
            LOGGER.warn("Failed to create image for vertex: " + userVertex.getId());
            new IOException(e);
        }
    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }
}
