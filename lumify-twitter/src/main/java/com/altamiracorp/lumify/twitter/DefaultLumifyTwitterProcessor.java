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

import static com.altamiracorp.lumify.twitter.TwitterConstants.*;

import com.altamiracorp.bigtable.model.FlushFlag;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.BaseArtifactProcessor;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRegexFinder;
import com.altamiracorp.lumify.core.json.JsonProperty;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.LabelName;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.securegraph.*;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

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
     * The list of properties modified during entity extraction.
     */
    private static final List<String> ENTITY_MODIFIED_PROPERTIES = Arrays.asList(
            PropertyName.TITLE.toString(),
            PropertyName.ROW_KEY.toString(),
            PropertyName.CONCEPT_TYPE.toString()
    );
    
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
        String rowKey = ArtifactRowKey.build(jsonBytes).toString();
        
        ArtifactExtractedInfo artifact = new ArtifactExtractedInfo()
                .text(tweetText)
                .raw(jsonBytes)
                .mimeType(TWEET_ARTIFACT_MIME_TYPE)
                .rowKey(rowKey)
                .conceptType(CONCEPT_TWEET)
                .title(tweetText)
                .author(tweeterScreenName)
                .source(TWITTER_SOURCE)
                .process(processId);
        if (tweetCreatedAt != null) {
            artifact.setDate(new Date(tweetCreatedAt));
        }
        
        Vertex tweet = getArtifactRepository().saveArtifact(artifact, user);
        String tweetId = tweet.getId().toString();
        LOGGER.info("Saved Tweet to Accumulo and as Graph Vertex: %s", tweetId);
        
        List<String> modifiedProps = setOptionalProps(tweet, jsonTweet, OPTIONAL_TWEET_PROPERTY_MAP, visibility);
        if (!modifiedProps.isEmpty()) {
            AuditRepository auditRepo = getAuditRepository();
            for (String prop : modifiedProps) {
                auditRepo.auditEntityProperties(AuditAction.UPDATE.toString(), tweet, prop, processId, "", user);
            }
        }
        
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
        Iterator<Vertex> userIterator = graph.query(lumifyUser.getAuthorizations()).has(PropertyName.TITLE.toString(), screenName).vertices().iterator();
        if (!userIterator.hasNext()) {
            userVertex = graph.addVertex(visibility);
        } else {
            // TODO what happens if userIterator contains multiple users
            userVertex = userIterator.next();
        }
        
        List<String> modifiedProps = Lists.newArrayList(
                PropertyName.TITLE.toString(),
                PropertyName.CONCEPT_TYPE.toString()
        );
        userVertex.setProperty(PropertyName.TITLE.toString(), screenName, visibility);
        userVertex.setProperty(PropertyName.CONCEPT_TYPE.toString(), handleConcept.getId(), visibility);
        
        modifiedProps.addAll(setOptionalProps(userVertex, jsonUser, OPTIONAL_USER_PROPERTY_MAP, visibility));

        AuditRepository auditRepo = getAuditRepository();
        for (String prop : modifiedProps) {
            auditRepo.auditEntityProperties(AuditAction.UPDATE.toString(), userVertex, prop, processId, "", lumifyUser);
        }
        
        // create the relationship between the user and their tweet
        graph.addEdge(tweetVertex, userVertex, TWEETED_RELATIONSHIP, visibility);
        String labelDispName = getOntologyRepository().getDisplayNameForLabel(TWEETED_RELATIONSHIP);
        auditRepo.auditRelationships(AuditAction.CREATE.toString(), userVertex, tweetVertex, labelDispName, processId, "", lumifyUser);
        
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
            String conceptId = concept.getId().toString();
            Vertex conceptVertex = graph.getVertex(conceptId, user.getAuthorizations());
            String relLabel = entityType.getRelationshipLabel();
            String relDispName = ontRepo.getDisplayNameForLabel(relLabel);
            
            List<TermMentionModel> mentions = TermRegexFinder.find(tweetId, conceptVertex, tweetText, entityType.getTermRegex());
            for (TermMentionModel mention : mentions) {
                String sign = mention.getMetadata().getSign().toLowerCase();
                String rowKey = mention.getRowKey().toString();

                boolean newVertex = false;
                Vertex termVertex = null;
                Iterator<Vertex> userIterator = graph.query(user.getAuthorizations()).has(PropertyName.TITLE.toString(), sign).vertices().iterator();
                if (!userIterator.hasNext()) {
                    termVertex = graph.addVertex(visibility);
                    newVertex = true;
                } else {
                    // TODO what happens if userIterator contains multiple users
                    termVertex = userIterator.next();
                }

                termVertex.setProperty(PropertyName.TITLE.toString(), sign, visibility);
                termVertex.setProperty(PropertyName.ROW_KEY.toString(), rowKey, visibility);
                termVertex.setProperty(PropertyName.CONCEPT_TYPE.toString(), conceptId, visibility);

                String termId = termVertex.getId().toString();
                if (newVertex) {
                    auditRepo.auditEntity(AuditAction.CREATE.toString(), termId, tweetVertex.getId().toString(),
                            sign, conceptId, processId, "", user);
                }
                for (String prop : ENTITY_MODIFIED_PROPERTIES) {
                    auditRepo.auditEntityProperties(AuditAction.UPDATE.toString(), termVertex, prop, processId, "", user);
                }
                
                mention.getMetadata().setVertexId(termId);
                getTermMentionRepository().save(mention, user.getModelUserContext());
                
                graph.addEdge(tweetVertex, termVertex, entityType.getRelationshipLabel(), visibility);
                auditRepo.auditRelationships(AuditAction.CREATE.toString(), tweetVertex, termVertex, relDispName, processId, "", user);
            }
        }
    }

    @Override
    public void retrieveProfileImage(final String processId, final JSONObject jsonTweet, final Vertex tweeterVertex) {
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
                String rowKey = ArtifactRowKey.build(rawImg).toString();

                ArtifactExtractedInfo artifactInfo = new ArtifactExtractedInfo()
                        .mimeType(PROFILE_IMAGE_MIME_TYPE)
                        .rowKey(rowKey)
                        .conceptType(CONCEPT_TWITTER_PROFILE_IMAGE)
                        .title(String.format(IMAGE_ARTIFACT_TITLE_FMT, screenName))
                        .source(IMAGE_ARTIFACT_SOURCE)
                        .process(processId)
                        .raw(rawImg);

                User user = getUser();
                Graph graph = getGraph();
                AuditRepository auditRepo = getAuditRepository();

                Vertex imageVertex = getArtifactRepository().saveArtifact(artifactInfo, user);

                LOGGER.debug("Saved Twitter User [%s] Profile Photo to Accumulo and as graph vertex: %s", screenName, imageVertex.getId());
                String labelDisplay = getOntologyRepository().getDisplayNameForLabel(LabelName.HAS_IMAGE.toString());
                auditRepo.auditRelationships(AuditAction.CREATE.toString(), tweeterVertex, imageVertex, labelDisplay, processId, "", user);

                tweeterVertex.setProperty(PropertyName.GLYPH_ICON.toString(), String.format(GLYPH_ICON_FMT, imageVertex.getId()), visibility);
                imageVertex.setProperty(PropertyName.GLYPH_ICON.toString(), String.format(GLYPH_ICON_FMT, imageVertex.getId()), visibility);
                auditRepo.auditEntityProperties(AuditAction.UPDATE.toString(), tweeterVertex, PropertyName.GLYPH_ICON.toString(),
                        processId, "", user);
                auditRepo.auditEntityProperties(AuditAction.UPDATE.toString(), imageVertex, PropertyName.GLYPH_ICON.toString(),
                        processId, "", user);

                Iterator<Edge> edges = tweeterVertex.getEdges(imageVertex, Direction.IN, LabelName.HAS_IMAGE.toString(), user.getAuthorizations()).iterator();
                if (!edges.hasNext()) {
                    graph.addEdge(tweeterVertex, imageVertex, LabelName.HAS_IMAGE.toString(), visibility);
                }

            } catch (MalformedURLException mue) {
                LOGGER.warn("Invalid Profile Photo URL [%s] for Twitter User: %s", imageUrl, screenName, mue);
            } catch (IOException ioe) {
                LOGGER.warn("Unable to retrieve Profile Photo [%s] for Twitter User: %s", imageUrl, screenName, ioe);
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
     * @param vertex the target vertex
     * @param srcObj the JSON object containing the property values
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
