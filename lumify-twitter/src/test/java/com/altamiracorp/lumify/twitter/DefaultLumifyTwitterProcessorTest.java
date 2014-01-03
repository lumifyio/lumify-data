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

import backtype.storm.tuple.Tuple;
import com.altamiracorp.bigtable.model.user.ModelUserContext;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.artifact.ArtifactType;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.core.user.User;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import java.util.Date;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Abstract base class for testing Twitter bolts.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultLumifyTwitterProcessorTest {
    private static final String TEST_PROCESS_ID = "twitter-processor-test";
    private static final String TEST_USER_SCREEN_NAME = "jota_2705";
    private static final String TEST_USER_NAME = "El grinder se vac\\u00edaa";
    private static final Integer TEST_USER_STATUS_COUNT = 3977;
    private static final Integer TEST_USER_FOLLOWERS_COUNT = 362;
    private static final Integer TEST_USER_FRIENDS_COUNT = 480;
    // "Fri May 18 14:48:35 +0000 2012"
    private static final Date TEST_USER_CREATED = new Date(1337352515000L);
    private static final String TEST_USER_DESCRIPTION =
            "Ya desde peque\\u00f1o supe dar ah\\u00ed donde hac\\u00eda da\\u00f1o." +
                    "Y no me refiero al tabaco,cateto, yo respeto al que consume y consumo con " +
                    "sumo respeto. R.A.P para to'desde el 99";
    private static final String TEST_USER_PROFILE_IMAGE_URL =
            "http://pbs.twimg.com/profile_images/412310266856996864/955IBes8_normal.jpeg";
    private static final Geoshape TEST_USER_COORDS = Geoshape.point(38.8951d, -77.0367d);
    private static final String TEST_TWEET_TEXT =
            "I'm at Target (2300 W. Ben White Blvd., S. Lamar Blvd., Austin) w\\/ 3 others http://t.co/eGSHZkXH #shopping";
    // "Thu Dec 19 22:07:04 +0000 2013"
    private static final Date TEST_TWEET_CREATED = new Date(1387490824000L);
    private static final Geoshape TEST_TWEET_COORDS = Geoshape.point(30.2500d, -97.7500d);
    private static final Integer TEST_TWEET_FAVORITE_COUNT = 42;
    private static final Integer TEST_TWEET_RETWEET_COUNT = 27;
    
    private static final String TEST_TWEET_VERTEX_ID = "testTweetVertex";
    private static final String TEST_TWEETER_VERTEX_ID = "testTweeterVertex";
    
    private static JSONObject FULL_USER;
    private static JSONObject FULL_TWEET;
    private static byte[] FULL_TWEET_BYTES;
    
    @Mock
    private Tuple tuple;
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
        
        when(user.getModelUserContext()).thenReturn(modelUserContext);
        when(ontologyRepository.getConceptByName(CONCEPT_TWITTER_HANDLE, user)).thenReturn(handleConcept);
        when(ontologyRepository.getConceptByName(CONCEPT_TWITTER_URL, user)).thenReturn(urlConcept);
        when(ontologyRepository.getConceptByName(CONCEPT_TWITTER_HASHTAG, user)).thenReturn(hashtagConcept);
        when(artifactRepository.findByRowKey(anyString(), any(ModelUserContext.class))).thenReturn(null);
        when(tweetVertex.getId()).thenReturn(TEST_TWEET_VERTEX_ID);
        when(tweeterVertex.getId()).thenReturn(TEST_TWEETER_VERTEX_ID);
    }
    
    @Test
    public void testParseTweet_NullJSON() {
        doShortCircuitTest(null);
    }
    
    @Test
    public void testParseTweet_NoText() {
        doShortCircuitTest(new JSONObject());
    }
    
    @Test
    public void testParseTweet_NoUser() {
        JSONObject tweet = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(tweet, TEST_TWEET_TEXT);
        doShortCircuitTest(tweet);
    }
    
    @Test
    public void testParseTweet_NoScreenName() {
        JSONObject tweet = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(tweet, TEST_TWEET_TEXT);
        JSON_USER_PROPERTY.setOn(tweet, new JSONObject());
        doShortCircuitTest(tweet);
    }
    
    @Test
    public void testParseTweet_EmptyScreenName() {
        JSONObject userJson = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(userJson, "");
        JSONObject tweet = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(tweet, TEST_TWEET_TEXT);
        JSON_USER_PROPERTY.setOn(tweet, userJson);
        doShortCircuitTest(tweet);
    }
    
    @Test
    public void testParseTweet_WhitespaceScreenName() {
        JSONObject userJson = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(userJson, "\n \t\t \n");
        JSONObject tweet = new JSONObject();
        JSON_TEXT_PROPERTY.setOn(tweet, TEST_TWEET_TEXT);
        JSON_USER_PROPERTY.setOn(tweet, userJson);
        doShortCircuitTest(tweet);
    }
    
    private void doShortCircuitTest(final JSONObject input) {
        GraphVertex vertex = instance.parseTweet(TEST_PROCESS_ID, input);
        assertNull(vertex);
        verify(artifactRepository, times(0)).saveArtifact(any(ArtifactExtractedInfo.class), any(User.class));
        verify(graphRepository, times(0)).save(any(GraphVertex.class), any(User.class));
        verify(auditRepository, times(0)).auditEntityProperties(anyString(), any(GraphVertex.class), anyString(), anyString(), anyString(),
                any(User.class));
    }
    
    @Test
    public void testParseTweet_NoOptionalProperties() {
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
                .artifactType(ArtifactType.DOCUMENT.toString())
                .title(TEST_TWEET_TEXT)
                .author(TEST_USER_SCREEN_NAME)
                .source("Twitter")
                .process(TEST_PROCESS_ID);
        
        when(artifactRepository.saveArtifact(eq(expectedArtifactInfo), eq(user))).thenReturn(tweetVertex);
        
        GraphVertex vertex = instance.parseTweet(TEST_PROCESS_ID, tweet);
        
        verify(graphRepository, times(0)).save(any(GraphVertex.class), any(User.class));
        verify(auditRepository, times(0)).auditEntityProperties(anyString(), any(GraphVertex.class), anyString(), anyString(), anyString(),
                any(User.class));
        assertEquals(tweetVertex, vertex);
    }
    
    @Test
    public void testParseTweet_SomeOptionalProperties() {
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
                .artifactType(ArtifactType.DOCUMENT.toString())
                .title(TEST_TWEET_TEXT)
                .author(TEST_USER_SCREEN_NAME)
                .source("Twitter")
                .process(TEST_PROCESS_ID)
                .date(TEST_TWEET_CREATED);
        
        when(artifactRepository.saveArtifact(eq(expectedArtifactInfo), eq(user))).thenReturn(tweetVertex);
        
        GraphVertex vertex = instance.parseTweet(TEST_PROCESS_ID, tweet);
        
        verify(tweetVertex).setProperty(eq(PropertyName.GEO_LOCATION.toString()), eq(TEST_TWEET_COORDS));
        verify(tweetVertex).setProperty(eq(LUMIFY_FAVORITE_COUNT_PROPERTY), eq(TEST_TWEET_FAVORITE_COUNT));
        verify(graphRepository).save(eq(tweetVertex), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweetVertex),
                eq(PropertyName.GEO_LOCATION.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweetVertex),
                eq(LUMIFY_FAVORITE_COUNT_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
        assertEquals(tweetVertex, vertex);
    }
    
    @Test
    public void testParseTweet_AllOptionalProperties() {
        String rowKey = ArtifactRowKey.build(FULL_TWEET_BYTES).toString();
        ArtifactExtractedInfo expectedArtifactInfo = new ArtifactExtractedInfo()
                .text(TEST_TWEET_TEXT)
                .raw(FULL_TWEET_BYTES)
                .mimeType("text/plain")
                .rowKey(rowKey)
                .artifactType(ArtifactType.DOCUMENT.toString())
                .title(TEST_TWEET_TEXT)
                .author(TEST_USER_SCREEN_NAME)
                .source("Twitter")
                .process(TEST_PROCESS_ID)
                .date(TEST_TWEET_CREATED);
        
        when(artifactRepository.saveArtifact(eq(expectedArtifactInfo), eq(user))).thenReturn(tweetVertex);
        
        GraphVertex vertex = instance.parseTweet(TEST_PROCESS_ID, FULL_TWEET);
        
        verify(tweetVertex).setProperty(eq(PropertyName.GEO_LOCATION.toString()), eq(TEST_TWEET_COORDS));
        verify(tweetVertex).setProperty(eq(LUMIFY_FAVORITE_COUNT_PROPERTY), eq(TEST_TWEET_FAVORITE_COUNT));
        verify(tweetVertex).setProperty(eq(LUMIFY_RETWEET_COUNT_PROPERTY), eq(TEST_TWEET_RETWEET_COUNT));
        verify(graphRepository).save(eq(tweetVertex), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweetVertex),
                eq(PropertyName.GEO_LOCATION.toString()), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweetVertex),
                eq(LUMIFY_FAVORITE_COUNT_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
        verify(auditRepository).auditEntityProperties(eq(AuditAction.UPDATE.toString()), eq(tweetVertex),
                eq(LUMIFY_RETWEET_COUNT_PROPERTY), eq(TEST_PROCESS_ID), anyString(), eq(user));
        assertEquals(tweetVertex, vertex);
    }
    
    private JSONObject buildScreenNameOnlyUser() {
        JSONObject obj = new JSONObject();
        JSON_SCREEN_NAME_PROPERTY.setOn(obj, TEST_USER_SCREEN_NAME);
        return obj;
    }
}
