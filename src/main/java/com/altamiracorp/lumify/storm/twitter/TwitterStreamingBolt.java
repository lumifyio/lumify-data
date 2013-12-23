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
import com.beust.jcommander.internal.Lists;
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
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TwitterStreamingBolt extends BaseLumifyBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterStreamingBolt.class);
    private static final String PROCESS = TwitterStreamingBolt.class.getName();
    public static final String TWITTER_HANDLE = "twitterHandle";
    private static final String TWEETED = "twitterHandleTweetedTweet";
    private static final String TWEET_MENTION = "tweetMentionedHandle";
    private static final String TWEET_HASHTAG = "tweetHasHashtag";
    private static final String TWEET_URL = "tweetHasURL";
    public static final String HASHTAG_CONCEPT = "hashtag";
    public static final String URL_CONCEPT = "url";
    private static final String FAVORITE_COUNT = "favoriteCount";
    private static final String RETWEET_COUNT = "retweetCount";
    private static final String STATUS_COUNT = "statusCount";
    private static final String FOLLOWER_COUNT = "followerCount";
    private static final String FOLLOWING_COUNT = "followingCount";
    private static final String CREATION_DATE = "creationDate";
    private static final String DESCRIPTION = "description";
    private Concept handleConcept;
    private SearchProvider searchProvider;
    private GraphVertex tweet;
    private String text;

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
        artifactExtractedInfo.setAuthor(tweeter);
        artifactExtractedInfo.setSource(source);

        Date date = formatCreatedAt(createdAt);
        if (date != null) {
            artifactExtractedInfo.setDate(date);
        }

        // Write to accumulo and create graph vertex for artifact
        tweet = saveArtifact(artifactExtractedInfo);
        LOGGER.info("Saving tweet to accumulo and as graph vertex: " + tweet.getId());

        String tweetId = tweet.getId();
        List<String> modifiedProperties = new ArrayList<String>();

        if (json.has("coordinates") && !json.get("coordinates").equals(JSONObject.NULL)) {
            JSONArray coordinates = json.getJSONObject("coordinates").getJSONArray("coordinates");
            Geoshape geo = Geoshape.point(coordinates.getDouble(1), coordinates.getDouble(0));
            tweet.setProperty(PropertyName.GEO_LOCATION, geo);
            modifiedProperties.add(PropertyName.GEO_LOCATION.toString());
        }

        if (json.has("favorite_count") && ((Integer) json.get("favorite_count") > 0)) {
            tweet.setProperty(FAVORITE_COUNT, json.get("favorite_count"));
            modifiedProperties.add(FAVORITE_COUNT);
        }

        if (json.has("retweet_count") && ((Integer) json.get("retweet_count") > 0)) {
            tweet.setProperty(RETWEET_COUNT, json.get("retweet_count"));
            modifiedProperties.add(RETWEET_COUNT);
        }

        graphRepository.save(tweet, getUser());
        auditRepository.audit(tweetId, auditRepository.vertexPropertyAuditMessages(tweet, modifiedProperties), getUser());

        createOrUpdateTweeterEntity(handleConcept, (JSONObject) json.get("user"));
    }

    private void createOrUpdateTweeterEntity(Concept handleConcept, JSONObject user) {
        String tweeter = user.getString("screen_name").toLowerCase();
        boolean newVertex = false;
        GraphVertex tweeterVertex = graphRepository.findVertexByTitleAndType(tweeter, VertexType.ENTITY, getUser());
        if (tweeterVertex == null) {
            newVertex = true;
            tweeterVertex = new InMemoryGraphVertex();
        }

        List<String> modifiedProperties = Lists.newArrayList
                (PropertyName.TITLE.toString(), PropertyName.TYPE.toString(), PropertyName.SUBTYPE.toString(), PropertyName.DISPLAY_NAME.toString());

        tweeterVertex.setProperty(PropertyName.TITLE, tweeter);
        tweeterVertex.setProperty(PropertyName.TYPE, VertexType.ENTITY.toString());
        tweeterVertex.setProperty(PropertyName.SUBTYPE, handleConcept.getId());
        tweeterVertex.setProperty(PropertyName.DISPLAY_NAME, user.getString("name"));
        graphRepository.save(tweeterVertex, getUser());

        if (newVertex) {
            auditRepository.audit(tweet.getId(), auditRepository.resolvedEntityAuditMessageForArtifact(tweeter), getUser());
            auditRepository.audit(tweeterVertex.getId(), auditRepository.resolvedEntityAuditMessage(tweet.getProperty(PropertyName.TITLE.toString())), getUser());
        }

        modifiedProperties.addAll(addHandleProperties(user, tweeterVertex));
        modifiedProperties.addAll(createProfilePhotoArtifact(user, tweeterVertex));

        auditRepository.audit(tweeterVertex.getId(), auditRepository.vertexPropertyAuditMessages(tweeterVertex, modifiedProperties), getUser());

        graphRepository.saveRelationship(tweeterVertex.getId(), tweet.getId(), TWEETED, getUser());
        String relationshipLabelDisplayName = ontologyRepository.getDisplayNameForLabel(TWEETED, getUser());
        auditRepository.audit(tweeterVertex.getId(), auditRepository.relationshipAuditMessageOnSource(relationshipLabelDisplayName, text, ""), getUser());
        auditRepository.audit(tweet.getId(), auditRepository.relationshipAuditMessageOnDest(relationshipLabelDisplayName, tweeter, text), getUser());
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
        GraphVertex conceptVertex = graphRepository.findVertex(concept.getId(), getUser());
        List<TermMention> termMentionList = TermRegexFinder.find(tweet.getId(), conceptVertex, text, regex);
        List<String> modifiedProperties = Lists.newArrayList
                (PropertyName.TITLE.toString(), PropertyName.ROW_KEY.toString(), PropertyName.TYPE.toString(), PropertyName.SUBTYPE.toString());

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

            vertex.setProperty(PropertyName.TITLE, sign);
            vertex.setProperty(PropertyName.ROW_KEY, rowKey);
            vertex.setProperty(PropertyName.TYPE, VertexType.ENTITY.toString());
            vertex.setProperty(PropertyName.SUBTYPE, conceptId);
            graphRepository.save(vertex, getUser());

            if (newVertex) {
                auditRepository.audit(tweet.getId(), auditRepository.resolvedEntityAuditMessageForArtifact(sign), getUser());
                auditRepository.audit(vertex.getId(), auditRepository.resolvedEntityAuditMessage(tweet.getProperty(PropertyName.TITLE.toString())), getUser());
            }
            auditRepository.audit(vertex.getId(), auditRepository.vertexPropertyAuditMessages(vertex, modifiedProperties), getUser());

            mention.getMetadata().setGraphVertexId(vertex.getId());
            termMentionRepository.save(mention, getUser().getModelUserContext());

            graphRepository.saveRelationship(tweet.getId(), vertex.getId(), relationshipLabel, getUser());
            String relationshipDisplayName = ontologyRepository.getDisplayNameForLabel(relationshipLabel, getUser());
            auditRepository.audit(tweet.getId(), auditRepository.relationshipAuditMessageOnSource(relationshipDisplayName, sign, ""), getUser());
            auditRepository.audit(vertex.getId(), auditRepository.relationshipAuditMessageOnDest(relationshipDisplayName, text, ""), getUser());
        }

    }

    public List<String> addHandleProperties(JSONObject user, GraphVertex handleVertex) {
        List<String> modifiedProperties = new ArrayList<String>();
        if (user.has("coordinates") && !user.get("coordinates").equals(JSONObject.NULL)) {
            JSONArray coordinates = user.getJSONObject("coordinates").getJSONArray("coordinates");
            handleVertex.setProperty(PropertyName.GEO_LOCATION, new GraphGeoLocation(coordinates.getDouble(1), coordinates.getDouble(0)));
            modifiedProperties.add(PropertyName.GEO_LOCATION.toString());
        }

        if (user.has("statuses_count") && ((Integer) user.get("statuses_count") > 0)) {
            String tweetCount = user.get("statuses_count").toString();
            handleVertex.setProperty(STATUS_COUNT, tweetCount);
            modifiedProperties.add(STATUS_COUNT);
        }

        if (user.has("followers_count") && ((Integer) user.get("followers_count") > 0)) {
            String followersCount = user.get("followers_count").toString();
            handleVertex.setProperty(FOLLOWER_COUNT, followersCount);
            modifiedProperties.add(FOLLOWER_COUNT);
        }

        if (user.has("friends_count") && ((Integer) user.get("friends_count") > 0)) {
            String friendsCount = user.get("friends_count").toString();
            handleVertex.setProperty(FOLLOWING_COUNT, friendsCount);
            modifiedProperties.add(FOLLOWING_COUNT);
        }

        String createdAt = user.has("created_at") ? user.getString("created_at") : null;
        Date date = formatCreatedAt(createdAt);
        if (date != null) {
            handleVertex.setProperty(CREATION_DATE, date.getTime());
            modifiedProperties.add(CREATION_DATE);
        }

        if (user.has("description") && !user.get("description").equals(JSONObject.NULL)) {
            handleVertex.setProperty(DESCRIPTION, user.getString("description"));
            modifiedProperties.add(DESCRIPTION);
        }
        graphRepository.save(handleVertex, getUser());
        return modifiedProperties;
    }

    public List<String> createProfilePhotoArtifact(JSONObject user, GraphVertex userVertex) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        List<String> modifiedProperties = new ArrayList<String>();
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

            GraphVertex profile = saveArtifact(artifactExtractedInfo);
            LOGGER.info("Saving tweeter profile picture to accumulo and as graph vertex: " + profile.getId());

            userVertex.setProperty(PropertyName.GLYPH_ICON.toString(), "/artifact/" + rowKey + "/raw");
            modifiedProperties.add(PropertyName.GLYPH_ICON.toString());
            graphRepository.save(userVertex, getUser());

            graphRepository.findOrAddRelationship(userVertex.getId(), profile.getId(), LabelName.HAS_IMAGE, getUser());

            String labelDisplay = ontologyRepository.getDisplayNameForLabel(LabelName.HAS_IMAGE.toString(), getUser());
            Object sourceTitle = userVertex.getProperty(PropertyName.TITLE.toString());
            Object destTitle = profile.getProperty(PropertyName.TITLE.toString());
            auditRepository.audit(userVertex.getId(), auditRepository.relationshipAuditMessageOnSource(labelDisplay, destTitle, text), getUser());
            auditRepository.audit(profile.getId(), auditRepository.relationshipAuditMessageOnDest(labelDisplay, sourceTitle, text), getUser());
            auditRepository.audit(tweet.getId(), auditRepository.relationshipAuditMessageOnArtifact(sourceTitle, destTitle, labelDisplay), getUser());
        } catch (IOException e) {
            LOGGER.warn("Failed to create image for vertex: " + userVertex.getId());
            new IOException(e);
        }
        return modifiedProperties;
    }

    public Date formatCreatedAt(String createdAt) {
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
            return date;
        }
        return null;
    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }
}
