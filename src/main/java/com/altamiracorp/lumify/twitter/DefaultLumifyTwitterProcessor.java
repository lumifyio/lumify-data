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

import com.altamiracorp.bigtable.model.FlushFlag;
import com.altamiracorp.lumify.core.ingest.BaseArtifactProcessor;
import com.altamiracorp.lumify.core.json.JsonProperty;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.properties.LumifyProperties;
import com.altamiracorp.lumify.core.model.properties.LumifyProperty;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.RowKeyHelper;
import com.altamiracorp.securegraph.*;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.*;

import static com.altamiracorp.lumify.core.model.ontology.OntologyLumifyProperties.CONCEPT_TYPE;
import static com.altamiracorp.lumify.core.model.properties.EntityLumifyProperties.GEO_LOCATION;
import static com.altamiracorp.lumify.core.model.properties.EntityLumifyProperties.SOURCE;
import static com.altamiracorp.lumify.core.model.properties.LumifyProperties.*;
import static com.altamiracorp.lumify.core.model.properties.RawLumifyProperties.*;
import static com.altamiracorp.lumify.twitter.TwitterConstants.*;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of the LumifyTwitterProcessor.
 */
public class DefaultLumifyTwitterProcessor extends BaseArtifactProcessor implements LumifyTwitterProcessor {
    /**
     * The class logger.
     */
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(DefaultLumifyTwitterProcessor.class);

    /**
     * The MIME type of Twitter artifacts.
     */
    private static final String TWEET_ARTIFACT_MIME_TYPE = "text/plain";

    /**
     * The source of Twitter artifacts.
     */
    private static final String TWITTER_SOURCE = "Twitter";

    /**
     * The profile image MIME type.
     */
    private static final String PROFILE_IMAGE_MIME_TYPE = "image/png";

    /**
     * The image artifact title format string.
     */
    private static final String IMAGE_ARTIFACT_TITLE_FMT = "%s Twitter Profile Picture";

    /**
     * The image artifact source.
     */
    private static final String IMAGE_ARTIFACT_SOURCE = "Twitter Profile Picture";

    /**
     * The glyph icon property value format.
     */
    private static final String GLYPH_ICON_FMT = "/artifact/%s/raw";

    /**
     * The Map of Lumify property keys to optional properties to extract
     * from a Tweet JSONObject.
     */
    @SuppressWarnings("unchecked")
    private static final Map<LumifyProperty, JsonProperty> OPTIONAL_TWEET_PROPERTY_MAP;

    /**
     * The Map of Lumify property keys to optional properties to extract
     * from a Twitter User JSONObject.
     */
    @SuppressWarnings("unchecked")
    private static final Map<LumifyProperty, JsonProperty> OPTIONAL_USER_PROPERTY_MAP;

    private static final String TWITTER_USER_PREFIX = "twitter-user-";
    private static final String TWITTER_HASHTAG_PREFIX = "twitter-hashtag-";
    private static final String TWITTER_URL_PREFIX = "twitter-url-";

    /**
     * Initialize the Optional Property maps.
     */
    static {
        @SuppressWarnings("unchecked")
        Map<LumifyProperty, JsonProperty> optTweetMap = new HashMap<LumifyProperty, JsonProperty>();
        optTweetMap.put(GEO_LOCATION, JSON_COORDINATES_PROPERTY);
        optTweetMap.put(LUMIFY_FAVORITE_COUNT_PROPERTY, JSON_FAVORITE_COUNT_PROPERTY);
        optTweetMap.put(LUMIFY_RETWEET_COUNT_PROPERTY, JSON_RETWEET_COUNT_PROPERTY);
        OPTIONAL_TWEET_PROPERTY_MAP = Collections.unmodifiableMap(optTweetMap);

        @SuppressWarnings("unchecked")
        Map<LumifyProperty, JsonProperty> optUserMap = new HashMap<LumifyProperty, JsonProperty>();
        optUserMap.put(DISPLAY_NAME, JSON_DISPLAY_NAME_PROPERTY);
        optUserMap.put(GEO_LOCATION, JSON_COORDINATES_PROPERTY);
        optUserMap.put(LUMIFY_STATUS_COUNT_PROPERTY, JSON_STATUS_COUNT_PROPERTY);
        optUserMap.put(LUMIFY_FOLLOWER_COUNT_PROPERTY, JSON_FOLLOWERS_COUNT_PROPERTY);
        optUserMap.put(LUMIFY_FOLLOWING_COUNT_PROPERTY, JSON_FRIENDS_COUNT_PROPERTY);
        optUserMap.put(LUMIFY_CREATION_DATE_PROPERTY, JSON_CREATED_AT_PROPERTY);
        optUserMap.put(LUMIFY_DESCRIPTION_PROPERTY, JSON_DESCRIPTION_PROPERTY);
        OPTIONAL_USER_PROPERTY_MAP = Collections.unmodifiableMap(optUserMap);
    }

    /**
     * The URL Stream Creator.
     */
    private UrlStreamCreator urlStreamCreator;

    @Override
    public void queueTweet(final String queueName, final JSONObject tweet) {
        if (queueName != null && !queueName.trim().isEmpty() && tweet != null) {
            getWorkQueueRepository().pushOnQueue(queueName.trim(), FlushFlag.DEFAULT, tweet);
        }
    }

    @Override
    public Vertex parseTweet(final String processId, final JSONObject jsonTweet) throws Exception {
        // TODO set visibility
        Visibility visibility = new Visibility("");

        // cache current User
        User user = getUser();

        String tweetText = JSON_TEXT_PROPERTY.getFrom(jsonTweet);
        Date tweetCreatedAt = JSON_CREATED_AT_PROPERTY.getFrom(jsonTweet);
        String tweeterScreenName = JSON_SCREEN_NAME_PROPERTY.getFrom(JSON_USER_PROPERTY.getFrom(jsonTweet));

        // at minimum, the tweet text and user screen name must be set or this object cannot be
        // added to the system as a Tweet
        if (tweetText == null || tweeterScreenName == null || tweeterScreenName.trim().isEmpty()) {
            return null;
        }

        byte[] jsonBytes = jsonTweet.toString().getBytes(TWITTER_CHARSET);
        String rowKey = RowKeyHelper.buildSHA256KeyString(jsonBytes);


        Concept concept = getOntologyRepository().getConceptByName(CONCEPT_TWEET);
        ElementMutation<Vertex> artifactMutation = findOrPrepareArtifactVertex(rowKey);
        CONCEPT_TYPE.setProperty(artifactMutation, concept.getId(), visibility);
        TITLE.setProperty(artifactMutation, tweetText, visibility);
        SOURCE.setProperty(artifactMutation, TWITTER_SOURCE, visibility);
        AUTHOR.setProperty(artifactMutation, tweeterScreenName, visibility);
        MIME_TYPE.setProperty(artifactMutation, TWEET_ARTIFACT_MIME_TYPE, visibility);
        ROW_KEY.setProperty(artifactMutation, rowKey, visibility);
        PROCESS.setProperty(artifactMutation, processId, visibility);

        if (tweetCreatedAt != null) {
            PUBLISHED_DATE.setProperty(artifactMutation, tweetCreatedAt, visibility);
        }

        TEXT.setProperty(artifactMutation, new StreamingPropertyValue(new ByteArrayInputStream(tweetText.getBytes(TWITTER_CHARSET)),
                String.class), visibility);

        setOptionalProps(artifactMutation, jsonTweet, OPTIONAL_TWEET_PROPERTY_MAP, visibility);

        Vertex tweet;
        if (!(artifactMutation instanceof ExistingElementMutation)) {
            tweet = artifactMutation.save();
            getAuditRepository().auditVertexElementMutation(artifactMutation, tweet, processId, user);
        } else {
            getAuditRepository().auditVertexElementMutation(artifactMutation, null, processId, user);
            tweet = artifactMutation.save();
        }
        getGraph().flush();

        String tweetId = tweet.getId().toString();
        LOGGER.info("Saved Tweet to Accumulo and as Graph Vertex: %s", tweetId);

        getAuditRepository().auditVertexElementMutation(artifactMutation, tweet, processId, user);

        return tweet;
    }

    @Override
    public Vertex parseTwitterUser(final String processId, final JSONObject jsonTweet, final Vertex tweetVertex) {
        // TODO set visibility
        Visibility visibility = new Visibility("");
        JSONObject jsonUser = JSON_USER_PROPERTY.getFrom(jsonTweet);
        String screenName = JSON_SCREEN_NAME_PROPERTY.getFrom(jsonUser);
        if (jsonUser == null || screenName == null || screenName.trim().isEmpty()) {
            return null;
        }

        // cache the current Lumify User
        User lumifyUser = getUser();
        Graph graph = getGraph();

        Concept handleConcept = getOntologyRepository().getConceptByName(CONCEPT_TWITTER_HANDLE);

        String id = TWITTER_USER_PREFIX + jsonUser.get("id");
        Vertex userVertex = graph.getVertex(id, lumifyUser.getAuthorizations());
        ElementMutation<Vertex> userVertexMutation;
        if (userVertex == null) {
            userVertexMutation = graph.prepareVertex(id, visibility, lumifyUser.getAuthorizations());
        } else {
            // TODO what happens if userIterator contains multiple users
            userVertexMutation = userVertex.prepareMutation();
        }

        TITLE.setProperty(userVertexMutation, screenName, visibility);
        CONCEPT_TYPE.setProperty(userVertexMutation, handleConcept.getId(), visibility);

        setOptionalProps(userVertexMutation, jsonUser, OPTIONAL_USER_PROPERTY_MAP, visibility);

        if (!(userVertexMutation instanceof ExistingElementMutation)) {
            userVertex = userVertexMutation.save();
            getAuditRepository().auditVertexElementMutation(userVertexMutation, userVertex, processId, lumifyUser);
        } else {
            getAuditRepository().auditVertexElementMutation(userVertexMutation, userVertex, processId, lumifyUser);
            userVertex = userVertexMutation.save();
        }

        // create the relationship between the user and their tweet
        graph.addEdge(tweetVertex, userVertex, TWEETED_RELATIONSHIP, visibility, lumifyUser.getAuthorizations());
        String labelDispName = getOntologyRepository().getDisplayNameForLabel(TWEETED_RELATIONSHIP);
        getAuditRepository().auditRelationship(AuditAction.CREATE, userVertex, tweetVertex, labelDispName, processId, "", lumifyUser);

        return userVertex;
    }

    @Override
    public void extractEntities(final String processId, final JSONObject jsonTweet, final Vertex tweetVertex,
                                final TwitterEntityType entityType) {
        // TODO set visibility
        Visibility visibility = new Visibility("");
        String tweetText = JSON_TEXT_PROPERTY.getFrom(jsonTweet);
        // only process if text is found in the tweet
        if (tweetText != null && !tweetText.trim().isEmpty()) {
            String tweetId = tweetVertex.getId().toString();
            User user = getUser();
            Graph graph = getGraph();
            OntologyRepository ontRepo = getOntologyRepository();
            AuditRepository auditRepo = getAuditRepository();

            Concept concept = ontRepo.getConceptByName(entityType.getConceptName());
            Vertex conceptVertex = concept.getVertex();
            String relDispName = conceptVertex.getPropertyValue(LumifyProperties.DISPLAY_NAME.getKey(), 0).toString();

            JSONArray entities = jsonTweet.getJSONObject("entities").getJSONArray(entityType.getJsonKey());
            List<TermMentionModel> mentions = new ArrayList<TermMentionModel>();

            for (int i = 0; i < entities.length(); i++) {
                JSONObject entity = entities.getJSONObject(i);
                String id;
                String sign = entity.getString(entityType.getSignKey());
                if (entityType.getConceptName().equals(CONCEPT_TWITTER_MENTION)) {
                    id = TWITTER_USER_PREFIX + entity.get("id");
                } else if (entityType.getConceptName().equals(CONCEPT_TWITTER_HASHTAG)) {
                    sign = sign.toLowerCase();
                    id = TWITTER_HASHTAG_PREFIX + sign;
                } else {
                    try {
                        id = URLEncoder.encode(TWITTER_URL_PREFIX + sign, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException("URL id could not be UTF-8 encoded");
                    }
                }
                checkNotNull(sign, "Term sign cannot be null");
                JSONArray indices = entity.getJSONArray("indices");
                TermMentionRowKey termMentionRowKey = new TermMentionRowKey(tweetId, indices.getLong(0), indices.getLong(1));
                TermMentionModel termMention = new TermMentionModel(termMentionRowKey);
                termMention.getMetadata()
                        .setSign(sign)
                        .setOntologyClassUri((String) conceptVertex.getPropertyValue(LumifyProperties.DISPLAY_NAME.getKey(), 0))
                        .setConceptGraphVertexId(concept.getId())
                        .setVertexId(id);
                mentions.add(termMention);
            }

            for (TermMentionModel mention : mentions) {
                String sign = mention.getMetadata().getSign().toLowerCase();
                String rowKey = mention.getRowKey().toString();
                String id = mention.getMetadata().getGraphVertexId();

                ElementMutation<Vertex> termVertexMutation;
                Vertex termVertex = graph.getVertex(id, user.getAuthorizations());
                if (termVertex == null) {
                    termVertexMutation = graph.prepareVertex(id, visibility, user.getAuthorizations());
                } else {
                    termVertexMutation = termVertex.prepareMutation();
                }

                TITLE.setProperty(termVertexMutation, sign, visibility);
                ROW_KEY.setProperty(termVertexMutation, rowKey, visibility);
                CONCEPT_TYPE.setProperty(termVertexMutation, concept.getId(), visibility);

                if (!(termVertexMutation instanceof ExistingElementMutation)) {
                    termVertex = termVertexMutation.save();
                    getAuditRepository().auditVertexElementMutation(termVertexMutation, termVertex, processId, user);
                } else {
                    getAuditRepository().auditVertexElementMutation(termVertexMutation, termVertex, processId, user);
                    termVertex = termVertexMutation.save();
                }

                String termId = termVertex.getId().toString();

                mention.getMetadata().setVertexId(termId);
                getTermMentionRepository().save(mention, user.getModelUserContext());

                graph.addEdge(tweetVertex, termVertex, entityType.getRelationshipLabel(), visibility, user.getAuthorizations());

                auditRepo.auditRelationship(AuditAction.CREATE, tweetVertex, termVertex, relDispName, processId, "", user);
                graph.flush();
            }
        }
    }

    @Override
    public void retrieveProfileImage(final String processId, final JSONObject jsonTweet, Vertex tweeterVertex) {
        Visibility visibility = new Visibility("");
        JSONObject tweeter = JSON_USER_PROPERTY.getFrom(jsonTweet);
        String screenName = JSON_SCREEN_NAME_PROPERTY.getFrom(tweeter);
        if (screenName != null) {
            screenName = screenName.trim();
        }
        String imageUrl = JSON_PROFILE_IMAGE_URL_PROPERTY.getFrom(tweeter);
        if (imageUrl != null) {
            imageUrl = imageUrl.trim();
        }
        if (screenName != null && !screenName.isEmpty() && imageUrl != null && !imageUrl.isEmpty()) {
            try {
                InputStream imgIn = urlStreamCreator.openUrlStream(imageUrl);
                ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
                IOUtils.copy(imgIn, imgOut);

                byte[] rawImg = imgOut.toByteArray();
                String rowKey = RowKeyHelper.buildSHA256KeyString(rawImg);

                User user = getUser();
                Graph graph = getGraph();
                AuditRepository auditRepo = getAuditRepository();
                StreamingPropertyValue raw = new StreamingPropertyValue(new ByteArrayInputStream(rawImg), byte[].class);
                raw.searchIndex(false);

                Concept concept = getOntologyRepository().getConceptByName(CONCEPT_TWITTER_PROFILE_IMAGE);
                ElementMutation<Vertex> imageBuilder = findOrPrepareArtifactVertex(rowKey);
                CONCEPT_TYPE.setProperty(imageBuilder, concept.getId(), visibility);
                TITLE.setProperty(imageBuilder, String.format(IMAGE_ARTIFACT_TITLE_FMT, screenName), visibility);
                SOURCE.setProperty(imageBuilder, IMAGE_ARTIFACT_SOURCE, visibility);
                MIME_TYPE.setProperty(imageBuilder, PROFILE_IMAGE_MIME_TYPE, visibility);
                PROCESS.setProperty(imageBuilder, processId, visibility);
                RAW.setProperty(imageBuilder, raw, visibility);

                Vertex imageVertex = null;
                if (!(imageBuilder instanceof ExistingElementMutation)) {
                    imageVertex = imageBuilder.save();
                    auditRepo.auditVertexElementMutation(imageBuilder, imageVertex, processId, user);
                } else {
                    auditRepo.auditVertexElementMutation(imageBuilder, imageVertex, processId, user);
                    imageVertex = imageBuilder.save();
                }

                LOGGER.debug("Saved Twitter User [%s] Profile Photo to Accumulo and as graph vertex: %s", screenName, imageVertex.getId());
                String labelDisplay = getOntologyRepository().getDisplayNameForLabel(ENTITY_HAS_IMAGE_HANDLE_PHOTO);
                auditRepo.auditRelationship(AuditAction.CREATE, tweeterVertex, imageVertex, labelDisplay, processId, "", user);

                // TO-DO: Replace GLYPH_ICON with ENTITY_IMAGE_URL
                ElementMutation<Vertex> tweeterVertexMutation = tweeterVertex.prepareMutation();
                tweeterVertexMutation.setProperty(GLYPH_ICON.getKey(), new Text(String.format(GLYPH_ICON_FMT, imageVertex.getId()), TextIndexHint.EXACT_MATCH), visibility);
                imageBuilder.setProperty(GLYPH_ICON.getKey(), new Text(String.format(GLYPH_ICON_FMT, imageVertex.getId()), TextIndexHint.EXACT_MATCH), visibility);

                auditRepo.auditVertexElementMutation(tweeterVertexMutation, tweeterVertex, processId, user);
                auditRepo.auditVertexElementMutation(imageBuilder, imageVertex, processId, user);

                tweeterVertex = tweeterVertexMutation.save();
                imageVertex = imageBuilder.save();

                Iterator<Edge> edges = tweeterVertex.getEdges(imageVertex, Direction.IN, ENTITY_HAS_IMAGE_HANDLE_PHOTO, user.getAuthorizations()).iterator();
                if (!edges.hasNext()) {
                    String displayName = getOntologyRepository().getDisplayNameForLabel(ENTITY_HAS_IMAGE_HANDLE_PHOTO);
                    graph.addEdge(tweeterVertex, imageVertex, ENTITY_HAS_IMAGE_HANDLE_PHOTO, visibility, user.getAuthorizations());
                    auditRepo.auditRelationship(AuditAction.CREATE, tweeterVertex, imageVertex, displayName, processId, "", user);
                }
                graph.flush();
            } catch (MalformedURLException mue) {
                LOGGER.warn("Invalid Profile Photo URL [%s] for Twitter User [%s]: %s", imageUrl, screenName, mue.getMessage());
            } catch (IOException ioe) {
                LOGGER.warn("BLB [%s] for Twitter User [%s]: %s", imageUrl, screenName, ioe.getMessage());
            }
        }
    }

    @Override
    public void finalizeTweetVertex(final String processId, final String tweetVertexId) {
        if (tweetVertexId != null) {
            getWorkQueueRepository().pushArtifactHighlight(tweetVertexId);
        }
    }

    @Inject
    public void setUrlStreamCreator(final UrlStreamCreator urlCreator) {
        urlStreamCreator = urlCreator;
    }

    /**
     * Sets optional properties on a Vertex, returning all property keys that were
     * modified.
     *
     * @param vertex     the target vertex
     * @param srcObj     the JSON object containing the property values
     * @param optProps   the map of Lumify property key to JsonProperty used to extract the value from the source object
     * @param visibility the visibility for all optional properties
     */
    @SuppressWarnings("unchecked")
    // we don't know the generic types of each Lumify and JsonProperty, need to use raw types
    private void setOptionalProps(final ElementMutation<Vertex> vertex, final JSONObject srcObj,
                                  final Map<LumifyProperty, JsonProperty> optProps, Visibility visibility) {
        for (Map.Entry<LumifyProperty, JsonProperty> optProp : optProps.entrySet()) {
            Object value = optProp.getValue().getFrom(srcObj);
            if (value != null) {
                optProp.getKey().setProperty(vertex, value, visibility);
            }
        }
    }
}
