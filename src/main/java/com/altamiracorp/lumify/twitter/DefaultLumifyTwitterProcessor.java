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
import com.altamiracorp.lumify.twitter.json.JsonProperty;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.properties.types.LumifyProperty;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.core.security.LumifyVisibility;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.RowKeyHelper;
import com.altamiracorp.securegraph.*;
import com.altamiracorp.securegraph.mutation.ElementMutation;
import com.altamiracorp.securegraph.mutation.ExistingElementMutation;
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
import static com.altamiracorp.securegraph.util.IterableUtils.toList;
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
        optUserMap.put(LUMIFY_PUBLISHED_DATE_PROPERTY, JSON_CREATED_AT_PROPERTY);
        optUserMap.put(LUMIFY_DESCRIPTION_PROPERTY, JSON_DESCRIPTION_PROPERTY);
        OPTIONAL_USER_PROPERTY_MAP = Collections.unmodifiableMap(optUserMap);
    }

    /**
     * The URL Stream Creator.
     */
    private UrlStreamCreator urlStreamCreator;

    private LumifyVisibility lumifyVisibility;

    @Override
    public void queueTweet(final String queueName, final JSONObject tweet) {
        lumifyVisibility = new LumifyVisibility();
        if (queueName != null && !queueName.trim().isEmpty() && tweet != null) {
            getWorkQueueRepository().pushOnQueue(queueName.trim(), FlushFlag.DEFAULT, tweet);
        }
    }

    @Override
    public Vertex parseTweet(final String processId, final JSONObject jsonTweet) throws Exception {
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

        Concept concept = getOntologyRepository().getConceptByIRI(CONCEPT_TWEET);
        ElementMutation<Vertex> artifactMutation = findOrPrepareArtifactVertex(rowKey, getAuthorizations());
        CONCEPT_TYPE.setProperty(artifactMutation, concept.getTitle(), lumifyVisibility.getVisibility());
        TITLE.setProperty(artifactMutation, tweetText, lumifyVisibility.getVisibility());
        SOURCE.setProperty(artifactMutation, TWITTER_SOURCE, lumifyVisibility.getVisibility());
        AUTHOR.setProperty(artifactMutation, tweeterScreenName, lumifyVisibility.getVisibility());
        MIME_TYPE.setProperty(artifactMutation, TWEET_ARTIFACT_MIME_TYPE, lumifyVisibility.getVisibility());
        ROW_KEY.setProperty(artifactMutation, rowKey, lumifyVisibility.getVisibility());
        PROCESS.setProperty(artifactMutation, processId, lumifyVisibility.getVisibility());

        if (tweetCreatedAt != null) {
            PUBLISHED_DATE.setProperty(artifactMutation, tweetCreatedAt, lumifyVisibility.getVisibility());
        }

        TEXT.setProperty(artifactMutation, new StreamingPropertyValue(new ByteArrayInputStream(tweetText.getBytes(TWITTER_CHARSET)),
                String.class), lumifyVisibility.getVisibility());

        setOptionalProps(artifactMutation, jsonTweet, OPTIONAL_TWEET_PROPERTY_MAP);

        Vertex tweet;
        if (!(artifactMutation instanceof ExistingElementMutation)) {
            tweet = artifactMutation.save();
            getAuditRepository().auditVertexElementMutation(AuditAction.UPDATE, artifactMutation, tweet, processId, user, lumifyVisibility.getVisibility());
        } else {
            getAuditRepository().auditVertexElementMutation(AuditAction.UPDATE, artifactMutation, null, processId, user, lumifyVisibility.getVisibility());
            tweet = artifactMutation.save();
        }
        getGraph().flush();

        String tweetId = tweet.getId().toString();
        LOGGER.info("Saved Tweet to Accumulo and as Graph Vertex: %s", tweetId);

        getAuditRepository().auditVertexElementMutation(AuditAction.UPDATE, artifactMutation, tweet, processId, user, lumifyVisibility.getVisibility());

        return tweet;
    }

    @Override
    public Vertex parseTwitterUser(final String processId, final JSONObject jsonTweet, final Vertex tweetVertex) {
        JSONObject jsonUser = JSON_USER_PROPERTY.getFrom(jsonTweet);
        String screenName = JSON_SCREEN_NAME_PROPERTY.getFrom(jsonUser);
        if (jsonUser == null || screenName == null || screenName.trim().isEmpty()) {
            return null;
        }

        // cache the current Lumify User
        User lumifyUser = getUser();
        Graph graph = getGraph();

        Concept handleConcept = getOntologyRepository().getConceptByIRI(CONCEPT_TWITTER_HANDLE);

        String id = TWITTER_USER_PREFIX + jsonUser.get("id");
        Vertex userVertex = graph.getVertex(id, getAuthorizations());
        ElementMutation<Vertex> userVertexMutation;
        if (userVertex == null) {
            userVertexMutation = graph.prepareVertex(id, lumifyVisibility.getVisibility(), getAuthorizations());
        } else {
            // TODO what happens if userIterator contains multiple users
            userVertexMutation = userVertex.prepareMutation();
        }

        TITLE.setProperty(userVertexMutation, screenName, lumifyVisibility.getVisibility());
        CONCEPT_TYPE.setProperty(userVertexMutation, handleConcept.getTitle(), lumifyVisibility.getVisibility());

        setOptionalProps(userVertexMutation, jsonUser, OPTIONAL_USER_PROPERTY_MAP);

        if (!(userVertexMutation instanceof ExistingElementMutation)) {
            userVertex = userVertexMutation.save();
            getAuditRepository().auditVertexElementMutation(AuditAction.UPDATE, userVertexMutation, userVertex, processId, lumifyUser, lumifyVisibility.getVisibility());
        } else {
            getAuditRepository().auditVertexElementMutation(AuditAction.UPDATE, userVertexMutation, userVertex, processId, lumifyUser, lumifyVisibility.getVisibility());
            userVertex = userVertexMutation.save();
        }

        // create the relationship between the user and their tweet
        Edge edge = graph.addEdge(tweetVertex, userVertex, TWEETED_RELATIONSHIP, lumifyVisibility.getVisibility(), getAuthorizations());
        getAuditRepository().auditRelationship(AuditAction.CREATE, userVertex, tweetVertex, edge, processId, "", lumifyUser, lumifyVisibility.getVisibility());

        return userVertex;
    }

    @Override
    public void extractEntities(final String processId, final JSONObject jsonTweet, final Vertex tweetVertex,
                                final TwitterEntityType entityType) {
        String tweetText = JSON_TEXT_PROPERTY.getFrom(jsonTweet);
        // only process if text is found in the tweet
        if (tweetText != null && !tweetText.trim().isEmpty()) {
            String tweetId = tweetVertex.getId().toString();
            User user = getUser();
            Graph graph = getGraph();
            OntologyRepository ontRepo = getOntologyRepository();
            AuditRepository auditRepo = getAuditRepository();

            Concept concept = ontRepo.getConceptByIRI(entityType.getConceptName());

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
                String propertyKey = "";
                TermMentionRowKey termMentionRowKey = new TermMentionRowKey(tweetId, propertyKey, indices.getLong(0), indices.getLong(1));
                TermMentionModel termMention = new TermMentionModel(termMentionRowKey);
                termMention.getMetadata()
                        .setSign(sign, lumifyVisibility.getVisibility())
                        .setOntologyClassUri(concept.getDisplayName(), lumifyVisibility.getVisibility())
                        .setConceptGraphVertexId(concept.getTitle(), lumifyVisibility.getVisibility())
                        .setVertexId(id, lumifyVisibility.getVisibility());
                mentions.add(termMention);
            }

            for (TermMentionModel mention : mentions) {
                String sign = mention.getMetadata().getSign().toLowerCase();
                String rowKey = mention.getRowKey().toString();
                String id = mention.getMetadata().getGraphVertexId();

                ElementMutation<Vertex> termVertexMutation;
                Vertex termVertex = graph.getVertex(id, getAuthorizations());
                if (termVertex == null) {
                    termVertexMutation = graph.prepareVertex(id, lumifyVisibility.getVisibility(), getAuthorizations());
                } else {
                    termVertexMutation = termVertex.prepareMutation();
                }

                TITLE.setProperty(termVertexMutation, sign, lumifyVisibility.getVisibility());
                ROW_KEY.setProperty(termVertexMutation, rowKey, lumifyVisibility.getVisibility());
                CONCEPT_TYPE.setProperty(termVertexMutation, concept.getTitle(), lumifyVisibility.getVisibility());

                if (!(termVertexMutation instanceof ExistingElementMutation)) {
                    termVertex = termVertexMutation.save();
                    getAuditRepository().auditVertexElementMutation(AuditAction.UPDATE, termVertexMutation, termVertex, processId, user, lumifyVisibility.getVisibility());
                } else {
                    getAuditRepository().auditVertexElementMutation(AuditAction.UPDATE, termVertexMutation, termVertex, processId, user, lumifyVisibility.getVisibility());
                    termVertex = termVertexMutation.save();
                }

                String termId = termVertex.getId().toString();

                mention.getMetadata().setVertexId(termId, lumifyVisibility.getVisibility());
                getTermMentionRepository().save(mention);

                Edge edge = graph.addEdge(tweetVertex, termVertex, entityType.getRelationshipLabel(), lumifyVisibility.getVisibility(), getAuthorizations());

                auditRepo.auditRelationship(AuditAction.CREATE, tweetVertex, termVertex, edge, processId, "", user, lumifyVisibility.getVisibility());
                graph.flush();
            }
        }
    }

    @Override
    public void retrieveProfileImage(final String processId, final JSONObject jsonTweet, Vertex tweeterVertex) {
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

                Concept concept = getOntologyRepository().getConceptByIRI(CONCEPT_TWITTER_PROFILE_IMAGE);
                ElementMutation<Vertex> imageBuilder = findOrPrepareArtifactVertex(rowKey, getAuthorizations());
                CONCEPT_TYPE.setProperty(imageBuilder, concept.getTitle(), lumifyVisibility.getVisibility());
                TITLE.setProperty(imageBuilder, String.format(IMAGE_ARTIFACT_TITLE_FMT, screenName), lumifyVisibility.getVisibility());
                SOURCE.setProperty(imageBuilder, IMAGE_ARTIFACT_SOURCE, lumifyVisibility.getVisibility());
                MIME_TYPE.setProperty(imageBuilder, PROFILE_IMAGE_MIME_TYPE, lumifyVisibility.getVisibility());
                PROCESS.setProperty(imageBuilder, processId, lumifyVisibility.getVisibility());
                RAW.setProperty(imageBuilder, raw, lumifyVisibility.getVisibility());

                Vertex imageVertex = null;
                if (!(imageBuilder instanceof ExistingElementMutation)) {
                    imageVertex = imageBuilder.save();
                    auditRepo.auditVertexElementMutation(AuditAction.UPDATE, imageBuilder, imageVertex, processId, user, lumifyVisibility.getVisibility());
                } else {
                    auditRepo.auditVertexElementMutation(AuditAction.UPDATE, imageBuilder, imageVertex, processId, user, lumifyVisibility.getVisibility());
                    imageVertex = imageBuilder.save();
                }

                LOGGER.debug("Saved Twitter User [%s] Profile Photo to Accumulo and as graph vertex: %s", screenName, imageVertex.getId());

                // TO-DO: Replace GLYPH_ICON with ENTITY_IMAGE_URL
                ElementMutation<Vertex> tweeterVertexMutation = tweeterVertex.prepareMutation();
                tweeterVertexMutation.setProperty(GLYPH_ICON.getKey(), new Text(String.format(GLYPH_ICON_FMT, imageVertex.getId()), TextIndexHint.EXACT_MATCH), lumifyVisibility.getVisibility());
                imageBuilder.setProperty(GLYPH_ICON.getKey(), new Text(String.format(GLYPH_ICON_FMT, imageVertex.getId()), TextIndexHint.EXACT_MATCH), lumifyVisibility.getVisibility());

                auditRepo.auditVertexElementMutation(AuditAction.UPDATE, tweeterVertexMutation, tweeterVertex, processId, user, lumifyVisibility.getVisibility());
                auditRepo.auditVertexElementMutation(AuditAction.UPDATE, imageBuilder, imageVertex, processId, user, lumifyVisibility.getVisibility());

                tweeterVertex = tweeterVertexMutation.save();
                imageVertex = imageBuilder.save();

                List<Edge> edges = toList(tweeterVertex.getEdges(imageVertex, Direction.IN, ENTITY_HAS_IMAGE_HANDLE_PHOTO, getAuthorizations()));
                if (edges.size() == 0) {
                    Edge edge = graph.addEdge(tweeterVertex, imageVertex, ENTITY_HAS_IMAGE_HANDLE_PHOTO, lumifyVisibility.getVisibility(), getAuthorizations());
                    auditRepo.auditRelationship(AuditAction.CREATE, tweeterVertex, imageVertex, edge, processId, "", user, lumifyVisibility.getVisibility());
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
    }

    @Inject
    public void setUrlStreamCreator(final UrlStreamCreator urlCreator) {
        urlStreamCreator = urlCreator;
    }

    /**
     * Sets optional properties on a Vertex, returning all property keys that were
     * modified.
     *
     * @param vertex   the target vertex
     * @param srcObj   the JSON object containing the property values
     * @param optProps the map of Lumify property key to JsonProperty used to extract the value from the source object
     */
    @SuppressWarnings("unchecked")
    // we don't know the generic types of each Lumify and JsonProperty, need to use raw types
    private void setOptionalProps(final ElementMutation<Vertex> vertex, final JSONObject srcObj,
                                  final Map<LumifyProperty, JsonProperty> optProps) {
        for (Map.Entry<LumifyProperty, JsonProperty> optProp : optProps.entrySet()) {
            Object value = optProp.getValue().getFrom(srcObj);
            if (value != null) {
                optProp.getKey().setProperty(vertex, value, lumifyVisibility.getVisibility());
            }
        }
    }
}
