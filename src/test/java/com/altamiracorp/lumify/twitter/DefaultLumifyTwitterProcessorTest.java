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

import static com.altamiracorp.lumify.twitter.TwitterConstants.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;

import com.altamiracorp.bigtable.model.user.ModelUserContext;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRegexFinder;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.LabelName;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.model.termMention.TermMentionMetadata;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

/**
 * Abstract base class for testing Twitter bolts.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ArtifactRowKey.class, TermRegexFinder.class, DefaultLumifyTwitterProcessor.class })
public class DefaultLumifyTwitterProcessorTest {
    private static final String TEST_PROCESS_ID = "twitter-processor-test";
    private static final String TEST_QUEUE_NAME = "testQueue";
    private static final String TEST_USER_SCREEN_NAME = "jota_2705";
    private static final String TEST_USER_NAME = "El grinder se vac\\u00edaa";
    private static final Integer TEST_USER_STATUS_COUNT = 3977;
    private static final Integer TEST_USER_FOLLOWERS_COUNT = 362;
    private static final Integer TEST_USER_FRIENDS_COUNT = 480;
    // "Fri May 18 14:48:35 +0000 2012"
    private static final Long TEST_USER_CREATED = 1337352515000L;
    private static final String TEST_USER_DESCRIPTION =
            "Ya desde peque\\u00f1o supe dar ah\\u00ed donde hac\\u00eda da\\u00f1o." +
                    "Y no me refiero al tabaco,cateto, yo respeto al que consume y consumo con " +
                    "sumo respeto. R.A.P para to'desde el 99";
    private static final String TEST_USER_PROFILE_IMAGE_URL =
            "http://pbs.twimg.com/profile_images/412310266856996864/955IBes8_normal.jpeg";
    private static final Geoshape TEST_USER_COORDS = Geoshape.point(38.8951d, -77.0367d);
    private static final String TEST_TWEET_TEXT =
            "I'm at Target (2300 W. Ben White Blvd., S. Lamar Blvd., Austin) w\\/ 3 others http://t.co/eGSHZkXH #shopping";
    private static final byte[] TEST_TWEET_TEXT_BYTES = TEST_TWEET_TEXT.getBytes(TWITTER_CHARSET);
    // "Thu Dec 19 22:07:04 +0000 2013"
    private static final Long TEST_TWEET_CREATED = 1387490824000L;
    private static final Geoshape TEST_TWEET_COORDS = Geoshape.point(30.2500d, -97.7500d);
    private static final Integer TEST_TWEET_FAVORITE_COUNT = 42;
    private static final Integer TEST_TWEET_RETWEET_COUNT = 27;
    private static final TwitterEntityType TEST_ENTITY_TYPE = TwitterEntityType.MENTION;
    
    private static final String TWEET_VERTEX_ID = "testTweetVertex";
    private static final String TWEETER_VERTEX_ID = "testTweeterVertex";
    private static final String HANDLE_CONCEPT_ID = "handleConcept";
    private static final String HASHTAG_CONCEPT_ID = "hashtagConcept";
    private static final String URL_CONCEPT_ID = "urlConcept";
    private static final String TWEETED_RELATIONSHIP_LABEL = "tweetedLabel";
    private static final String MENTION_RELATIONSIHP_LABEL = "mentionedLabel";
    
    private static JSONObject FULL_USER;
    private static JSONObject FULL_TWEET;
    private static byte[] FULL_TWEET_BYTES;
    
    @Mock
    private LumifyLogger logger;
    @Mock
    private AuditRepository auditRepository;
    @Mock
    private User user;
    @Mock
    private OntologyRepository ontologyRepository;
    @Mock
    private ArtifactRepository artifactRepository;
    @Mock
    private GraphRepository graphRepository;
    @Mock
    private TermMentionRepository termMentionRepository;
    @Mock
    private WorkQueueRepository workQueueRepository;
    @Mock
    private UrlStreamCreator urlStreamCreator;
    @Mock
    private ModelUserContext modelUserContext;
    @Mock
    private Concept handleConcept;
    @Mock
    private Concept hashtagConcept;
    @Mock
    private Concept urlConcept;
    @Mock
    private GraphVertex tweetVertex;
    @Mock
    private GraphVertex tweeterVertex;
    @Mock
    private GraphVertex handleConceptVertex;
    
    private DefaultLumifyTwitterProcessor instance;
    
    @BeforeClass
    public static void setupClass() {
        FULL_USER = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(FULL_USER, TEST_USER_SCREEN_NAME);
        JSON_DISPLAY_NAME_PROPERTY.setOn(FULL_USER, TEST_USER_NAME);
        JSON_STATUS_COUNT_PROPERTY.setOn(FULL_USER, TEST_USER_STATUS_COUNT);
        JSON_FOLLOWERS_COUNT_PROPERTY.setOn(FULL_USER, TEST_USER_FOLLOWERS_COUNT);
        JSON_FRIENDS_COUNT_PROPERTY.setOn(FULL_USER, TEST_USER_FRIENDS_COUNT);
        JSON_CREATED_AT_PROPERTY.setOn(FULL_USER, TEST_USER_CREATED);
        JSON_DESCRIPTION_PROPERTY.setOn(FULL_USER, TEST_USER_DESCRIPTION);
        JSON_COORDINATES_PROPERTY.setOn(FULL_USER, TEST_USER_COORDS);
        JSON_PROFILE_IMAGE_URL_PROPERTY.setOn(FULL_USER, TEST_USER_PROFILE_IMAGE_URL);
        
        FULL_TWEET = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(FULL_TWEET, TEST_TWEET_TEXT);
        JSON_CREATED_AT_PROPERTY.setOn(FULL_TWEET, TEST_TWEET_CREATED);
        JSON_USER_PROPERTY.setOn(FULL_TWEET, FULL_USER);
        JSON_COORDINATES_PROPERTY.setOn(FULL_TWEET, TEST_TWEET_COORDS);
        JSON_FAVORITE_COUNT_PROPERTY.setOn(FULL_TWEET, TEST_TWEET_FAVORITE_COUNT);
        JSON_RETWEET_COUNT_PROPERTY.setOn(FULL_TWEET, TEST_TWEET_RETWEET_COUNT);
        
        FULL_TWEET_BYTES = FULL_TWEET.toString().getBytes(TWITTER_CHARSET);
    }
    
    @Before
    public void setup() throws Exception {
        instance = new DefaultLumifyTwitterProcessor();
        instance.setOntologyRepository(ontologyRepository);
        instance.setArtifactRepository(artifactRepository);
        instance.setGraphRepository(graphRepository);
        instance.setAuditRepository(auditRepository);
        instance.setUser(user);
        instance.setTermMentionRepository(termMentionRepository);
        instance.setWorkQueueRepository(workQueueRepository);
        instance.setUrlStreamCreator(urlStreamCreator);
        Whitebox.setInternalState(DefaultLumifyTwitterProcessor.class, logger);
        
        when(user.getModelUserContext()).thenReturn(modelUserContext);
        when(ontologyRepository.getConceptByName(CONCEPT_TWITTER_HANDLE, user)).thenReturn(handleConcept);
        when(ontologyRepository.getConceptByName(CONCEPT_TWITTER_URL, user)).thenReturn(urlConcept);
        when(ontologyRepository.getConceptByName(CONCEPT_TWITTER_HASHTAG, user)).thenReturn(hashtagConcept);
        when(ontologyRepository.getDisplayNameForLabel(TWEETED_RELATIONSHIP, user)).thenReturn(TWEETED_RELATIONSHIP_LABEL);
        when(ontologyRepository.getDisplayNameForLabel(TwitterEntityType.MENTION.getRelationshipLabel(), user)).
                thenReturn(MENTION_RELATIONSIHP_LABEL);
        when(graphRepository.findVertex(HANDLE_CONCEPT_ID, user)).thenReturn(handleConceptVertex);
        when(artifactRepository.findByRowKey(anyString(), any(ModelUserContext.class))).thenReturn(null);
        when(tweetVertex.getId()).thenReturn(TWEET_VERTEX_ID);
        when(tweeterVertex.getId()).thenReturn(TWEETER_VERTEX_ID);
        when(handleConcept.getId()).thenReturn(HANDLE_CONCEPT_ID);
        when(hashtagConcept.getId()).thenReturn(HASHTAG_CONCEPT_ID);
        when(urlConcept.getId()).thenReturn(URL_CONCEPT_ID);
    }
    
    @Test
    public void testQueueTweet_NullQueue() {
        doShortCircuitQueueTweetTest(null, FULL_TWEET);
    }
    
    @Test
    public void testQueueTweet_EmptyQueue() {
        doShortCircuitQueueTweetTest("", FULL_TWEET);
    }
    
    @Test
    public void testQueueTweet_WhitespaceQueue() {
        doShortCircuitQueueTweetTest("\n \t\t \n", FULL_TWEET);
    }
    
    @Test
    public void testQueueTweet_NullTweet() {
        doShortCircuitQueueTweetTest(TEST_QUEUE_NAME, null);
    }
    
    @Test
    public void testQueueTweet_TrimmedQueue() {
        instance.queueTweet(TEST_QUEUE_NAME, FULL_TWEET);
        verify(workQueueRepository).pushOnQueue(TEST_QUEUE_NAME, FULL_TWEET);
    }
    
    @Test
    public void testQueueTweet_UntrimmedQueue() {
        instance.queueTweet("\n \t\t " + TEST_QUEUE_NAME + " \n", FULL_TWEET);
        verify(workQueueRepository).pushOnQueue(TEST_QUEUE_NAME, FULL_TWEET);
    }
    
    private void doShortCircuitQueueTweetTest(final String queueName, final JSONObject tweet) {
        instance.queueTweet(queueName, tweet);
        verify(workQueueRepository, never()).pushOnQueue(anyString(), any(JSONObject.class));
    }
    
    @Test
    public void testParseTweet_NullJSON() throws Exception {
        doShortCircuitTweetTest(null);
    }
    
    @Test
    public void testParseTweet_NoText() throws Exception {
        doShortCircuitTweetTest(new JSONObject());
    }
    
    @Test
    public void testParseTweet_NoUser() throws Exception {
        JSONObject tweet = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(tweet, TEST_TWEET_TEXT);
        doShortCircuitTweetTest(tweet);
    }
    
    @Test
    public void testParseTweet_NoScreenName() throws Exception {
        JSONObject tweet = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(tweet, TEST_TWEET_TEXT);
        JSON_USER_PROPERTY.setOn(tweet, new JSONObject());
        doShortCircuitTweetTest(tweet);
    }
    
    @Test
    public void testParseTweet_EmptyScreenName() throws Exception {
        JSONObject userJson = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(userJson, "");
        JSONObject tweet = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(tweet, TEST_TWEET_TEXT);
        JSON_USER_PROPERTY.setOn(tweet, userJson);
        doShortCircuitTweetTest(tweet);
    }
    
    @Test
    public void testParseTweet_WhitespaceScreenName() throws Exception {
        JSONObject userJson = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(userJson, "\n \t\t \n");
        JSONObject tweet = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(tweet, TEST_TWEET_TEXT);
        JSON_USER_PROPERTY.setOn(tweet, userJson);
        doShortCircuitTweetTest(tweet);
    }
    
    @Test
    public void testParseTweet_NoOptionalProperties() throws Exception {
        JSONObject tweet = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(tweet, TEST_TWEET_TEXT);
        JSON_USER_PROPERTY.setOn(tweet, buildScreenNameOnlyUser());
        
        byte[] jsonBytes = tweet.toString().getBytes(TWITTER_CHARSET);
        String rowKey = ArtifactRowKey.build(jsonBytes).toString();
        ArtifactExtractedInfo expectedArtifactInfo = new ArtifactExtractedInfo()
                .text(TEST_TWEET_TEXT)
                .raw(jsonBytes)
                .mimeType("text/plain")
                .rowKey(rowKey)
                .conceptType(CONCEPT_TWEET)
                .title(TEST_TWEET_TEXT)
                .author(TEST_USER_SCREEN_NAME)
                .source("Twitter")
                .process(TEST_PROCESS_ID);
        
        when(artifactRepository.saveArtifact(expectedArtifactInfo, user)).thenReturn(tweetVertex);
        
        GraphVertex vertex = instance.parseTweet(TEST_PROCESS_ID, tweet);
        
        verify(graphRepository, never()).save(any(GraphVertex.class), any(User.class));
        verify(auditRepository, never()).auditEntityProperties(anyString(), any(GraphVertex.class), anyString(), anyString(), anyString(),
                any(User.class));
        assertEquals(tweetVertex, vertex);
    }
    
    @Test
    public void testParseTweet_SomeOptionalProperties() throws Exception {
        JSONObject tweet = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(tweet, TEST_TWEET_TEXT);
        JSON_CREATED_AT_PROPERTY.setOn(tweet, TEST_TWEET_CREATED);
        JSON_COORDINATES_PROPERTY.setOn(tweet, TEST_TWEET_COORDS);
        JSON_FAVORITE_COUNT_PROPERTY.setOn(tweet, TEST_TWEET_FAVORITE_COUNT);
        JSON_USER_PROPERTY.setOn(tweet, buildScreenNameOnlyUser());
        
        byte[] jsonBytes = tweet.toString().getBytes(TWITTER_CHARSET);
        String rowKey = ArtifactRowKey.build(jsonBytes).toString();
        ArtifactExtractedInfo expectedArtifactInfo = new ArtifactExtractedInfo()
                .text(TEST_TWEET_TEXT)
                .raw(jsonBytes)
                .mimeType("text/plain")
                .rowKey(rowKey)
                .conceptType(CONCEPT_TWEET)
                .title(TEST_TWEET_TEXT)
                .author(TEST_USER_SCREEN_NAME)
                .source("Twitter")
                .process(TEST_PROCESS_ID)
                .date(new Date(TEST_TWEET_CREATED));
        
        when(artifactRepository.saveArtifact(expectedArtifactInfo, user)).thenReturn(tweetVertex);
        
        GraphVertex vertex = instance.parseTweet(TEST_PROCESS_ID, tweet);
        
        verify(tweetVertex).setProperty(PropertyName.GEO_LOCATION.toString(), TEST_TWEET_COORDS);
        verify(tweetVertex).setProperty(LUMIFY_FAVORITE_COUNT_PROPERTY, TEST_TWEET_FAVORITE_COUNT);
        verify(graphRepository).save(tweetVertex, user);
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweetVertex),
                eq(PropertyName.GEO_LOCATION.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweetVertex),
                eq(LUMIFY_FAVORITE_COUNT_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
        assertEquals(tweetVertex, vertex);
    }
    
    @Test
    public void testParseTweet_AllOptionalProperties() throws Exception {
        String rowKey = ArtifactRowKey.build(FULL_TWEET_BYTES).toString();
        ArtifactExtractedInfo expectedArtifactInfo = new ArtifactExtractedInfo()
                .text(TEST_TWEET_TEXT)
                .raw(FULL_TWEET_BYTES)
                .mimeType("text/plain")
                .rowKey(rowKey)
                .conceptType(CONCEPT_TWEET)
                .title(TEST_TWEET_TEXT)
                .author(TEST_USER_SCREEN_NAME)
                .source("Twitter")
                .process(TEST_PROCESS_ID)
                .date(new Date(TEST_TWEET_CREATED));
        
        when(artifactRepository.saveArtifact(expectedArtifactInfo, user)).thenReturn(tweetVertex);
        
        GraphVertex vertex = instance.parseTweet(TEST_PROCESS_ID, FULL_TWEET);
        
        verify(tweetVertex).setProperty(PropertyName.GEO_LOCATION.toString(), TEST_TWEET_COORDS);
        verify(tweetVertex).setProperty(LUMIFY_FAVORITE_COUNT_PROPERTY, TEST_TWEET_FAVORITE_COUNT);
        verify(tweetVertex).setProperty(LUMIFY_RETWEET_COUNT_PROPERTY, TEST_TWEET_RETWEET_COUNT);
        verify(graphRepository).save(tweetVertex, user);
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweetVertex),
                eq(PropertyName.GEO_LOCATION.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweetVertex),
                eq(LUMIFY_FAVORITE_COUNT_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweetVertex),
                eq(LUMIFY_RETWEET_COUNT_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
        assertEquals(tweetVertex, vertex);
    }
    
    @Test
    public void testParseTwitterUser_NullJSON() {
        doShortCircuitUserTest(null);
    }
    
    @Test
    public void testParseTwitterUser_NoUser() {
        doShortCircuitUserTest(new JSONObject());
    }
    
    @Test
    public void testParseTwitterUser_NoScreenName() {
        JSONObject tweeter = new JSONObject();
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, tweeter);
        doShortCircuitUserTest(tweet);
    }
    
    @Test
    public void testParseTwitterUser_EmptyScreenName() {
        JSONObject tweeter = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(tweeter, "");
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, tweeter);
        doShortCircuitUserTest(tweet);
    }
    
    @Test
    public void testParseTwitterUser_WhitespaceScreenName() {
        JSONObject tweeter = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(tweeter, "\n \t\t \n");
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, tweeter);
        doShortCircuitUserTest(tweet);
    }
    
    @Test
    public void testParseTwitterUser_ScreenNameOnly() {
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, buildScreenNameOnlyUser());
        doSimpleUserTest(tweet);
    }
    
    @Test
    public void testParseTwitterUser_SomeOptionalProperties() {
        JSONObject tweeter = buildScreenNameOnlyUser();
        JSON_DISPLAY_NAME_PROPERTY.setOn(tweeter, TEST_USER_NAME);
        JSON_CREATED_AT_PROPERTY.setOn(tweeter, TEST_USER_CREATED);
        JSON_FRIENDS_COUNT_PROPERTY.setOn(tweeter, TEST_USER_FRIENDS_COUNT);
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, tweeter);
        
        doSimpleUserTest(tweet);
        
        verify(tweeterVertex).setProperty(PropertyName.DISPLAY_NAME.toString(), TEST_USER_NAME);
        verify(tweeterVertex).setProperty(LUMIFY_CREATION_DATE_PROPERTY, TEST_USER_CREATED);
        verify(tweeterVertex).setProperty(LUMIFY_FOLLOWING_COUNT_PROPERTY, TEST_USER_FRIENDS_COUNT);
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweeterVertex),
                eq(PropertyName.DISPLAY_NAME.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweeterVertex),
                eq(LUMIFY_CREATION_DATE_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweeterVertex),
                eq(LUMIFY_FOLLOWING_COUNT_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
    }
    
    @Test
    public void testParseTwitterUser_AllOptionalProperties() {
        doSimpleUserTest(FULL_TWEET);
        
        verify(tweeterVertex).setProperty(PropertyName.DISPLAY_NAME.toString(), TEST_USER_NAME);
        verify(tweeterVertex).setProperty(LUMIFY_CREATION_DATE_PROPERTY, TEST_USER_CREATED);
        verify(tweeterVertex).setProperty(LUMIFY_FOLLOWING_COUNT_PROPERTY, TEST_USER_FRIENDS_COUNT);
        verify(tweeterVertex).setProperty(PropertyName.GEO_LOCATION.toString(), TEST_USER_COORDS);
        verify(tweeterVertex).setProperty(LUMIFY_STATUS_COUNT_PROPERTY, TEST_USER_STATUS_COUNT);
        verify(tweeterVertex).setProperty(LUMIFY_FOLLOWER_COUNT_PROPERTY, TEST_USER_FOLLOWERS_COUNT);
        verify(tweeterVertex).setProperty(LUMIFY_DESCRIPTION_PROPERTY, TEST_USER_DESCRIPTION);
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweeterVertex),
                eq(PropertyName.DISPLAY_NAME.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweeterVertex),
                eq(LUMIFY_CREATION_DATE_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweeterVertex),
                eq(LUMIFY_FOLLOWING_COUNT_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweeterVertex),
                eq(PropertyName.GEO_LOCATION.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweeterVertex),
                eq(LUMIFY_STATUS_COUNT_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweeterVertex),
                eq(LUMIFY_FOLLOWER_COUNT_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweeterVertex),
                eq(LUMIFY_DESCRIPTION_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
    }
    
    @Test
    public void testParseTwitterUser_ScreenNameOnly_NewVertex() throws Exception {
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, buildScreenNameOnlyUser());
        
        when(graphRepository.findVertexByExactTitle(TEST_USER_SCREEN_NAME, user)).thenReturn(null);
        
        InMemoryGraphVertex newUser = mock(InMemoryGraphVertex.class);
        String newUserId = "newUserId";
        when(newUser.getId()).thenReturn(newUserId);
        PowerMockito.whenNew(InMemoryGraphVertex.class).withNoArguments().thenReturn(newUser);
        
        doSimpleUserTest(tweet, newUser, false);
    }
    
    @Test
    public void testExtractEntities_NullJSON() {
        doShortCircuitEntityTest(null);
    }
    
    @Test
    public void testExtractEntities_NoText() {
        doShortCircuitEntityTest(new JSONObject());
    }
    
    @Test
    public void testExtractEntities_EmptyText() {
        JSONObject tweet = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(tweet, "");
        doShortCircuitEntityTest(tweet);
    }
    
    @Test
    public void testExtractEntities_WhitespaceText() {
        JSONObject tweet = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(tweet, "\n \t\t \n");
        doShortCircuitEntityTest(tweet);
    }
    
    @Test
    public void testExtractEntities_NoEntitiesFound() {
        PowerMockito.mockStatic(TermRegexFinder.class);
        
        when(TermRegexFinder.find(TWEET_VERTEX_ID, handleConceptVertex, TEST_TWEET_TEXT, TEST_ENTITY_TYPE.getTermRegex())).
                thenReturn(Collections.EMPTY_LIST);
        
        instance.extractEntities(TEST_PROCESS_ID, FULL_TWEET, tweetVertex, TEST_ENTITY_TYPE);
        
        PowerMockito.verifyStatic();
        TermRegexFinder.find(TWEET_VERTEX_ID, handleConceptVertex, TEST_TWEET_TEXT, TEST_ENTITY_TYPE.getTermRegex());
        doNoTermVerifies();
    }
    
    @Test
    public void testExtractEntities_EntitiesFound() throws Exception {
        TermMentionModel mention1 = mock(TermMentionModel.class, "mention1");
        TermMentionModel mention2 = mock(TermMentionModel.class, "mention2");
        TermMentionMetadata md1 = mock(TermMentionMetadata.class, "md1");
        TermMentionMetadata md2 = mock(TermMentionMetadata.class, "md2");
        TermMentionRowKey rk1 = mock(TermMentionRowKey.class, "rk1");
        TermMentionRowKey rk2 = mock(TermMentionRowKey.class, "rk2");
        String sign1 = "sign1";
        String sign2 = "sign2";
        String key1 = "key1";
        String key2 = "key2";
        
        when(mention1.getMetadata()).thenReturn(md1);
        when(mention2.getMetadata()).thenReturn(md2);
        when(mention1.getRowKey()).thenReturn(rk1);
        when(mention2.getRowKey()).thenReturn(rk2);
        when(md1.getSign()).thenReturn(sign1);
        when(md2.getSign()).thenReturn(sign2);
        when(rk1.toString()).thenReturn(key1);
        when(rk2.toString()).thenReturn(key2);
        
        GraphVertex existingTerm = mock(GraphVertex.class, "existingTerm");
        InMemoryGraphVertex newTerm = mock(InMemoryGraphVertex.class, "newTerm");
        String existingTermId = "existingTermId";
        String newTermId = "newTermId";
        
        when(existingTerm.getId()).thenReturn(existingTermId);
        when(newTerm.getId()).thenReturn(newTermId);
        
        // sign1 is an existing term with a new mention
        when(graphRepository.findVertexByExactTitle(sign1, user)).thenReturn(existingTerm);
        // sign2 is a new term
        when(graphRepository.findVertexByExactTitle(sign2, user)).thenReturn(null);
        
        PowerMockito.mockStatic(TermRegexFinder.class);
        when(TermRegexFinder.find(TWEET_VERTEX_ID, handleConceptVertex, TEST_TWEET_TEXT, TEST_ENTITY_TYPE.getTermRegex())).
                thenReturn(Arrays.asList(mention1, mention2));
        PowerMockito.whenNew(InMemoryGraphVertex.class).withNoArguments().thenReturn(newTerm);
        
        instance.extractEntities(TEST_PROCESS_ID, FULL_TWEET, tweetVertex, TEST_ENTITY_TYPE);
        
        // verify existing term is updated with new mention
        verify(existingTerm).setProperty(PropertyName.TITLE, sign1);
        verify(existingTerm).setProperty(PropertyName.ROW_KEY, key1);
        verify(existingTerm).setProperty(PropertyName.CONCEPT_TYPE, HANDLE_CONCEPT_ID);
        verify(graphRepository).save(existingTerm, user);
        verify(auditRepository, never()).auditEntity(eq(AuditAction.CREATE.toString()), eq(existingTermId), eq(TWEET_VERTEX_ID),
                eq(sign1), eq(HANDLE_CONCEPT_ID), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(existingTerm),
                eq(PropertyName.TITLE.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(existingTerm),
                eq(PropertyName.ROW_KEY.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(existingTerm),
                eq(PropertyName.CONCEPT_TYPE.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(md1).setGraphVertexId(existingTermId);
        verify(termMentionRepository).save(mention1, modelUserContext);
        verify(graphRepository).saveRelationship(TWEET_VERTEX_ID, existingTermId, TEST_ENTITY_TYPE.getRelationshipLabel(), user);
        verify(auditRepository).auditRelationships(eq(AuditAction.CREATE.toString()), eq(tweetVertex), eq(existingTerm),
                eq(MENTION_RELATIONSIHP_LABEL), eq(TEST_PROCESS_ID), anyString(), eq(user));
        
        // verify new term is created
        verify(newTerm).setProperty(PropertyName.TITLE, sign2);
        verify(newTerm).setProperty(PropertyName.ROW_KEY, key2);
        verify(newTerm).setProperty(PropertyName.CONCEPT_TYPE, HANDLE_CONCEPT_ID);
        verify(graphRepository).save(newTerm, user);
        verify(auditRepository).auditEntity(eq(AuditAction.CREATE.toString()), eq(newTermId), eq(TWEET_VERTEX_ID),
                eq(sign2), eq(HANDLE_CONCEPT_ID), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(newTerm),
                eq(PropertyName.TITLE.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(newTerm),
                eq(PropertyName.ROW_KEY.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(newTerm),
                eq(PropertyName.CONCEPT_TYPE.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(md2).setGraphVertexId(newTermId);
        verify(termMentionRepository).save(mention2, modelUserContext);
        verify(graphRepository).saveRelationship(TWEET_VERTEX_ID, newTermId, TEST_ENTITY_TYPE.getRelationshipLabel(), user);
        verify(auditRepository).auditRelationships(eq(AuditAction.CREATE.toString()), eq(tweetVertex), eq(newTerm),
                eq(MENTION_RELATIONSIHP_LABEL), eq(TEST_PROCESS_ID), anyString(), eq(user));
        
    }
    
    @Test
    public void testRetrieveProfileImage_NoUser() throws Exception {
        JSONObject tweet = new JSONObject();
        doShortCircuitProfileImageTest(tweet);
    }
    
    @Test
    public void testRetrieveProfileImage_NoScreenName() throws Exception {
        JSONObject tweeter = new JSONObject();
        JSON_PROFILE_IMAGE_URL_PROPERTY.setOn(tweeter, TEST_USER_PROFILE_IMAGE_URL);
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, tweeter);
        doShortCircuitProfileImageTest(tweet);
    }
    
    @Test
    public void testRetrieveProfileImage_EmptyScreenName() throws Exception {
        JSONObject tweeter = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(tweeter, "");
        JSON_PROFILE_IMAGE_URL_PROPERTY.setOn(tweeter, TEST_USER_PROFILE_IMAGE_URL);
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, tweeter);
        doShortCircuitProfileImageTest(tweet);
    }
    
    @Test
    public void testRetrieveProfileImage_WhitespaceScreenName() throws Exception {
        JSONObject tweeter = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(tweeter, "\n \t\t \n");
        JSON_PROFILE_IMAGE_URL_PROPERTY.setOn(tweeter, TEST_USER_PROFILE_IMAGE_URL);
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, tweeter);
        doShortCircuitProfileImageTest(tweet);
    }
    
    @Test
    public void testRetrieveProfileImage_NoImageUrl() throws Exception {
        JSONObject tweeter = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(tweeter, TEST_USER_SCREEN_NAME);
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, tweeter);
        doShortCircuitProfileImageTest(tweet);
    }
    
    @Test
    public void testRetrieveProfileImage_EmptyImageUrl() throws Exception {
        JSONObject tweeter = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(tweeter, TEST_USER_SCREEN_NAME);
        JSON_PROFILE_IMAGE_URL_PROPERTY.setOn(tweeter, "");
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, tweeter);
        doShortCircuitProfileImageTest(tweet);
    }
    
    @Test
    public void testRetrieveProfileImage_WhitespaceImageUrl() throws Exception {
        JSONObject tweeter = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(tweeter, TEST_USER_SCREEN_NAME);
        JSON_PROFILE_IMAGE_URL_PROPERTY.setOn(tweeter, "\n \t\t \n");
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, tweeter);
        doShortCircuitProfileImageTest(tweet);
    }
    
    @Test
    public void testRetrieveProfileImage_MalformedURLException() throws Exception {
        doRetreiveProfileImageExceptionTest(MalformedURLException.class);
    }
    
    @Test
    public void testRetrieveProfileImage_IOException() throws Exception {
        doRetreiveProfileImageExceptionTest(IOException.class);
    }
    
    @Test
    public void testRetrieveProfileImage() throws Exception {
        JSONObject tweeter = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(tweeter, TEST_USER_SCREEN_NAME);
        JSON_PROFILE_IMAGE_URL_PROPERTY.setOn(tweeter, TEST_USER_PROFILE_IMAGE_URL);
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, tweeter);
        
        byte[] testImageBytes = "My Image Content".getBytes(TWITTER_CHARSET);
        ArtifactRowKey rowKey = mock(ArtifactRowKey.class);
        String rowKeyStr = "testRowKey";
        when(rowKey.toString()).thenReturn(rowKeyStr);
        
        when(urlStreamCreator.openUrlStream(TEST_USER_PROFILE_IMAGE_URL)).thenReturn(new ByteArrayInputStream(testImageBytes));
        PowerMockito.mockStatic(ArtifactRowKey.class);
        when(ArtifactRowKey.build(testImageBytes)).thenReturn(rowKey);
        
        ArtifactExtractedInfo expectedInfo = new ArtifactExtractedInfo()
                .mimeType("image/png")
                .rowKey(rowKeyStr)
                .conceptType(CONCEPT_TWITTER_PROFILE_IMAGE)
                .title(TEST_USER_SCREEN_NAME + " Twitter Profile Picture")
                .source("Twitter Profile Picture")
                .process(TEST_PROCESS_ID)
                .raw(testImageBytes);
        
        GraphVertex imageVertex = mock(GraphVertex.class);
        String imageVertexId = "testImageVertexId";
        when(imageVertex.getId()).thenReturn(imageVertexId);
        when(artifactRepository.saveArtifact(expectedInfo, user)).thenReturn(imageVertex);
        
        String hasImageLabel = "testHasImage";
        when(ontologyRepository.getDisplayNameForLabel(LabelName.HAS_IMAGE.toString(), user)).thenReturn(hasImageLabel);
        
        instance.retrieveProfileImage(TEST_PROCESS_ID, tweet, tweeterVertex);
        
        verify(tweeterVertex).setProperty(PropertyName.GLYPH_ICON.toString(), "/artifact/" + imageVertexId + "/raw");
        verify(graphRepository).save(tweeterVertex, user);
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweeterVertex),
                eq(PropertyName.GLYPH_ICON.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(graphRepository).findOrAddRelationship(TWEETER_VERTEX_ID, imageVertexId, LabelName.HAS_IMAGE.toString(), user);
        verify(auditRepository).auditRelationships(eq(AuditAction.CREATE.toString()), eq(tweeterVertex), eq(imageVertex),
                eq(hasImageLabel), eq(TEST_PROCESS_ID), anyString(), eq(user));
    }
    
    @Test
    public void testFinalizeTweetVertex_NullId() {
        instance.finalizeTweetVertex(TEST_PROCESS_ID, null);
        verify(workQueueRepository, never()).pushArtifactHighlight(anyString());
    }
    
    @Test
    public void testFinalizeTweetVertex() {
        instance.finalizeTweetVertex(TEST_PROCESS_ID, TWEET_VERTEX_ID);
        verify(workQueueRepository).pushArtifactHighlight(TWEET_VERTEX_ID);
    }
    
    private void doRetreiveProfileImageExceptionTest(final Class<? extends Throwable> throwableClass) throws Exception {
        String badUrl = "not a URL";
        JSONObject tweeter = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(tweeter, TEST_USER_SCREEN_NAME);
        JSON_PROFILE_IMAGE_URL_PROPERTY.setOn(tweeter, badUrl);
        JSONObject tweet = new JSONObject();
        JSON_USER_PROPERTY.setOn(tweet, tweeter);
        
        when(urlStreamCreator.openUrlStream(badUrl)).thenThrow(throwableClass);
        
        instance.retrieveProfileImage(TEST_PROCESS_ID, tweet, tweeterVertex);
        
        verify(urlStreamCreator).openUrlStream(anyString());
        verify(artifactRepository, never()).saveArtifact(any(ArtifactExtractedInfo.class), any(User.class));
        verify(tweeterVertex, never()).setProperty(anyString(), any(User.class));
        verify(graphRepository, never()).save(any(GraphVertex.class), any(User.class));
        verify(graphRepository, never()).findOrAddRelationship(anyString(), anyString(), anyString(), any(User.class));
        verify(ontologyRepository, never()).getDisplayNameForLabel(anyString(), any(User.class));
        verify(auditRepository, never()).auditEntityProperties(anyString(), any(GraphVertex.class), anyString(), anyString(), anyString(),
                any(User.class));
        verify(auditRepository, never()).auditRelationships(anyString(), any(GraphVertex.class), any(GraphVertex.class), anyString(),
                anyString(), anyString(), any(User.class));
        verify(logger).warn(anyString(), eq(badUrl), eq(TEST_USER_SCREEN_NAME), any(throwableClass));
    }
    
    private void doShortCircuitTweetTest(final JSONObject input) throws Exception {
        GraphVertex vertex = instance.parseTweet(TEST_PROCESS_ID, input);
        assertNull(vertex);
        verify(artifactRepository, never()).saveArtifact(any(ArtifactExtractedInfo.class), any(User.class));
        verify(graphRepository, never()).save(any(GraphVertex.class), any(User.class));
        verify(auditRepository, never()).auditEntityProperties(anyString(), any(GraphVertex.class), anyString(), anyString(), anyString(),
                any(User.class));
    }
    
    private void doShortCircuitUserTest(final JSONObject input) {
        GraphVertex vertex = instance.parseTwitterUser(TEST_PROCESS_ID, input, tweetVertex);
        assertNull(vertex);
        verify(ontologyRepository, never()).getConceptByName(anyString(), any(User.class));
        verify(graphRepository, never()).save(any(GraphVertex.class), any(User.class));
        verify(auditRepository, never()).auditEntityProperties(anyString(), any(GraphVertex.class), anyString(), anyString(), anyString(),
                any(User.class));
        verify(graphRepository, never()).saveRelationship(anyString(), anyString(), anyString(), any(User.class));
        verify(auditRepository, never()).auditRelationships(anyString(), any(GraphVertex.class), any(GraphVertex.class), anyString(),
                anyString(), anyString(), any(User.class));
    }
    
    private void doSimpleUserTest(final JSONObject input) {
        doSimpleUserTest(input, tweeterVertex, true);
    }
    
    private void doSimpleUserTest(final JSONObject input, final GraphVertex mockVertex, final boolean vertexExists) {
        when(graphRepository.findVertexByExactTitle(TEST_USER_SCREEN_NAME, user)).thenReturn(vertexExists ? mockVertex : null);
        
        GraphVertex vertex = instance.parseTwitterUser(TEST_PROCESS_ID, input, tweetVertex);
        
        assertEquals(mockVertex, vertex);
        verify(mockVertex).setProperty(PropertyName.TITLE, TEST_USER_SCREEN_NAME);
        verify(mockVertex).setProperty(PropertyName.CONCEPT_TYPE, HANDLE_CONCEPT_ID);
        verify(graphRepository).save(mockVertex, user);
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(mockVertex),
                eq(PropertyName.TITLE.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(mockVertex),
                eq(PropertyName.CONCEPT_TYPE.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(graphRepository).saveRelationship(mockVertex.getId(), TWEET_VERTEX_ID, TWEETED_RELATIONSHIP,
                user);
        verify(auditRepository).auditRelationships(eq(AuditAction.CREATE.toString()), eq(mockVertex), eq(tweetVertex),
                eq(TWEETED_RELATIONSHIP_LABEL), eq(TEST_PROCESS_ID), anyString(), eq(user));
    }
    
    private void doShortCircuitEntityTest(final JSONObject input) {
        PowerMockito.mockStatic(TermRegexFinder.class);
        
        instance.extractEntities(TEST_PROCESS_ID, input, tweetVertex, TEST_ENTITY_TYPE);
        verify(ontologyRepository, never()).getConceptByName(anyString(), any(User.class));
        verify(ontologyRepository, never()).getDisplayNameForLabel(anyString(), any(User.class));
        verify(graphRepository, never()).findVertex(anyString(), any(User.class));
        
        PowerMockito.verifyStatic(never());
        TermRegexFinder.find(anyString(), any(GraphVertex.class), anyString(), any(Pattern.class));
        
        doNoTermVerifies();
    }
    
    private void doNoTermVerifies() {
        verify(graphRepository, never()).findVertexByExactTitle(anyString(), any(User.class));
        verify(graphRepository, never()).save(any(GraphVertex.class), any(User.class));
        verify(graphRepository, never()).saveRelationship(anyString(), anyString(), anyString(), any(User.class));
        verify(termMentionRepository, never()).save(any(TermMentionModel.class), any(ModelUserContext.class));
        verify(auditRepository, never()).auditEntity(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), any(User.class));
        verify(auditRepository, never()).auditEntityProperties(anyString(), any(GraphVertex.class), anyString(), anyString(),
                anyString(), any(User.class));
        verify(auditRepository, never()).auditRelationships(anyString(), any(GraphVertex.class), any(GraphVertex.class), anyString(),
                anyString(), anyString(), any(User.class));
    }
    
    private void doShortCircuitProfileImageTest(final JSONObject input) throws Exception {
        instance.retrieveProfileImage(TEST_PROCESS_ID, input, tweeterVertex);
        
        verify(urlStreamCreator, never()).openUrlStream(anyString());
        verify(artifactRepository, never()).saveArtifact(any(ArtifactExtractedInfo.class), any(User.class));
        verify(tweeterVertex, never()).setProperty(anyString(), any(User.class));
        verify(graphRepository, never()).save(any(GraphVertex.class), any(User.class));
        verify(graphRepository, never()).findOrAddRelationship(anyString(), anyString(), anyString(), any(User.class));
        verify(ontologyRepository, never()).getDisplayNameForLabel(anyString(), any(User.class));
        verify(auditRepository, never()).auditEntityProperties(anyString(), any(GraphVertex.class), anyString(), anyString(), anyString(),
                any(User.class));
        verify(auditRepository, never()).auditRelationships(anyString(), any(GraphVertex.class), any(GraphVertex.class), anyString(),
                anyString(), anyString(), any(User.class));
        verify(logger, never()).warn(anyString(), any());
    }
    
    private JSONObject buildScreenNameOnlyUser() {
        JSONObject obj = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(obj, TEST_USER_SCREEN_NAME);
        return obj;
    }
}
