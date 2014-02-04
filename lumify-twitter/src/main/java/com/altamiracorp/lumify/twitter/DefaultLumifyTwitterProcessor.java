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
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRegexFinder;
import com.altamiracorp.lumify.core.json.JsonProperty;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.RowKeyHelper;
import com.altamiracorp.securegraph.*;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.*;

import static com.altamiracorp.lumify.twitter.TwitterConstants.*;

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
    private static final Map<String, JsonProperty<?, ?>> OPTIONAL_TWEET_PROPERTY_MAP;

    /**
     * The Map of Lumify property keys to optional properties to extract
     * from a Twitter User JSONObject.
     */
    private static final Map<String, JsonProperty<?, ?>> OPTIONAL_USER_PROPERTY_MAP;

    /**
     * Initialize the Optional Property maps.
     */
    static {
        Map<String, JsonProperty<?, ?>> optTweetMap = new HashMap<String, JsonProperty<?, ?>>();
        optTweetMap.put(PropertyName.GEO_LOCATION.toString(), JSON_COORDINATES_PROPERTY);
        optTweetMap.put(LUMIFY_FAVORITE_COUNT_PROPERTY, JSON_FAVORITE_COUNT_PROPERTY);
        optTweetMap.put(LUMIFY_RETWEET_COUNT_PROPERTY, JSON_RETWEET_COUNT_PROPERTY);
        OPTIONAL_TWEET_PROPERTY_MAP = Collections.unmodifiableMap(optTweetMap);

        Map<String, JsonProperty<?, ?>> optUserMap = new HashMap<String, JsonProperty<?, ?>>();
        optUserMap.put(PropertyName.DISPLAY_NAME.toString(), JSON_DISPLAY_NAME_PROPERTY);
        optUserMap.put(PropertyName.GEO_LOCATION.toString(), JSON_COORDINATES_PROPERTY);
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
        Long tweetCreatedAt = JSON_CREATED_AT_PROPERTY.getFrom(jsonTweet);
        String tweeterScreenName = JSON_SCREEN_NAME_PROPERTY.getFrom(JSON_USER_PROPERTY.getFrom(jsonTweet));

        // at minimum, the tweet text and user screen name must be set or this object cannot be
        // added to the system as a Tweet
        if (tweetText == null || tweeterScreenName == null || tweeterScreenName.trim().isEmpty()) {
            return null;
        }

        byte[] jsonBytes = jsonTweet.toString().getBytes(TWITTER_CHARSET);
        String rowKey = RowKeyHelper.buildSHA256KeyString(jsonBytes);


        Object conceptId = getOntologyRepository().getConceptByName(CONCEPT_TWEET).getId();
        if (conceptId instanceof String) {
            conceptId = new Text((String) conceptId, TextIndex.EXACT_MATCH);
        }
        ElementMutation<Vertex> artifactMutation = findOrPrepareArtifactVertex(rowKey)
                .setProperty(PropertyName.MIME_TYPE.toString(), new Text(TWEET_ARTIFACT_MIME_TYPE), visibility)
                .setProperty(PropertyName.CONCEPT_TYPE.toString(), conceptId, visibility)
                .setProperty(PropertyName.TITLE.toString(), new Text(tweetText), visibility)
                .setProperty(PropertyName.AUTHOR.toString(), new Text(tweeterScreenName), visibility)
                .setProperty(PropertyName.SOURCE.toString(), new Text(TWITTER_SOURCE), visibility)
                .setProperty(PropertyName.PROCESS.toString(), new Text(processId, TextIndex.EXACT_MATCH), visibility)
                .setProperty(PropertyName.ROW_KEY.toString(), new Text(rowKey, TextIndex.EXACT_MATCH), visibility);

        if (tweetCreatedAt != null) {
            artifactMutation.setProperty(PropertyName.PUBLISHED_DATE.toString(), new Date(tweetCreatedAt), visibility);
        }

        artifactMutation.setProperty(PropertyName.TEXT.toString(), new StreamingPropertyValue(new ByteArrayInputStream(tweetText.getBytes()), String.class), visibility);

        Vertex tweet = null;
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

        Vertex userVertex = null;
        ElementMutation<Vertex> userVertexMutation;
        Iterator<Vertex> userIterator = graph.query(lumifyUser.getAuthorizations()).has(PropertyName.TITLE.toString(), screenName).vertices().iterator();
        if (!userIterator.hasNext()) {
            userVertexMutation = graph.prepareVertex(visibility, lumifyUser.getAuthorizations());
        } else {
            // TODO what happens if userIterator contains multiple users
            userVertex = userIterator.next();
            userVertexMutation = userVertex.prepareMutation();
        }

        userVertexMutation.setProperty(PropertyName.TITLE.toString(), new Text(screenName), visibility);
        Object handleConceptId = handleConcept.getId();
        if (handleConceptId instanceof String) {
            handleConceptId = new Text((String) handleConceptId, TextIndex.EXACT_MATCH);
        }
        userVertexMutation.setProperty(PropertyName.CONCEPT_TYPE.toString(), handleConceptId, visibility);

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
            String relLabel = entityType.getRelationshipLabel();
            String relDispName = ontRepo.getDisplayNameForLabel(relLabel);

            List<TermMentionModel> mentions = TermRegexFinder.find(tweetId, conceptVertex, tweetText, entityType.getTermRegex());
            for (TermMentionModel mention : mentions) {
                String sign = mention.getMetadata().getSign().toLowerCase();
                String rowKey = mention.getRowKey().toString();

                Vertex termVertex = null;
                ElementMutation<Vertex> termVertexMutation;
                Iterator<Vertex> userIterator = graph.query(user.getAuthorizations()).has(PropertyName.TITLE.toString(), sign).vertices().iterator();
                if (!userIterator.hasNext()) {
                    termVertexMutation = graph.prepareVertex(visibility, user.getAuthorizations());
                } else {
                    // TODO what happens if userIterator contains multiple users
                    termVertex = userIterator.next();
                    termVertexMutation = termVertex.prepareMutation();
                }

                termVertexMutation.setProperty(PropertyName.TITLE.toString(), new Text(sign), visibility);
                termVertexMutation.setProperty(PropertyName.ROW_KEY.toString(), new Text(rowKey, TextIndex.EXACT_MATCH), visibility);
                Object conceptId = concept.getId();
                if (conceptId instanceof String) {
                    conceptId = new Text((String) conceptId, TextIndex.EXACT_MATCH);
                }
                termVertexMutation.setProperty(PropertyName.CONCEPT_TYPE.toString(), conceptId, visibility);

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

                Object conceptId = getOntologyRepository().getConceptByName(CONCEPT_TWITTER_PROFILE_IMAGE).getId();
                if (conceptId instanceof String) {
                    conceptId = new Text((String) conceptId, TextIndex.EXACT_MATCH);
                }
                ElementMutation<Vertex> imageBuilder = findOrPrepareArtifactVertex(rowKey)
                        .setProperty(PropertyName.MIME_TYPE.toString(), new Text(PROFILE_IMAGE_MIME_TYPE), visibility)
                        .setProperty(PropertyName.CONCEPT_TYPE.toString(), conceptId, visibility)
                        .setProperty(PropertyName.TITLE.toString(), new Text(String.format(IMAGE_ARTIFACT_TITLE_FMT, screenName)), visibility)
                        .setProperty(PropertyName.SOURCE.toString(), new Text(IMAGE_ARTIFACT_SOURCE), visibility)
                        .setProperty(PropertyName.PROCESS.toString(), new Text(processId), visibility)
                        .setProperty(PropertyName.RAW.toString(), raw, visibility);

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

                ElementMutation<Vertex> tweeterVertexMutation = tweeterVertex.prepareMutation();
                tweeterVertexMutation.setProperty(PropertyName.GLYPH_ICON.toString(), new Text(String.format(GLYPH_ICON_FMT, imageVertex.getId()), TextIndex.EXACT_MATCH), visibility);
                imageBuilder.setProperty(PropertyName.GLYPH_ICON.toString(), new Text(String.format(GLYPH_ICON_FMT, imageVertex.getId()), TextIndex.EXACT_MATCH), visibility);

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
     * @param vertex   the target vertex
     * @param srcObj   the JSON object containing the property values
     * @param optProps the map of Lumify property key to JsonProperty used to extract the value from the source object
     * @return the list of property keys that were modified
     */
    private List<String> setOptionalProps(final Vertex vertex, final JSONObject srcObj,
                                          final Map<String, JsonProperty<?, ?>> optProps, Visibility visibility) {
        List<String> modifiedProps = new ArrayList<String>(optProps.size());
        for (Map.Entry<String, JsonProperty<?, ?>> optProp : optProps.entrySet()) {
            Object value = optProp.getValue().getFrom(srcObj);
            if (value != null) {
                vertex.setProperty(optProp.getKey(), value, visibility);
                modifiedProps.add(optProp.getKey());
            }
        }
        return modifiedProps;
    }
}
