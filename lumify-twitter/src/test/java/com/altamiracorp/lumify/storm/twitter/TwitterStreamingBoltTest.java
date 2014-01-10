package com.altamiracorp.lumify.storm.twitter;

import com.altamiracorp.bigtable.model.user.ModelUserContext;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.user.User;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.junit.BeforeClass;

import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class TwitterStreamingBoltTest extends BaseTwitterBoltTest<TwitterStreamingBolt> {
    private static final String TEST_USER_SCREEN_NAME = "jota_2705";
    private static final String TEST_USER_NAME = "El grinder se vac\\u00edaa";
    private static final Integer TEST_USER_STATUS_COUNT = 3977;
    private static final Integer TEST_USER_FOLLOWERS_COUNT = 362;
    private static final Integer TEST_USER_FRIENDS_COUNT = 480;
    private static final String TEST_USER_CREATED = "Fri May 18 14:48:35 +0000 2012";
    private static final String TEST_USER_DESCRIPTION =
            "Ya desde peque\\u00f1o supe dar ah\\u00ed donde hac\\u00eda da\\u00f1o." +
                    "Y no me refiero al tabaco,cateto, yo respeto al que consume y consumo con " +
                    "sumo respeto. R.A.P para to'desde el 99";
    private static final String TEST_USER_PROFILE_IMAGE_URL =
            "http://pbs.twimg.com/profile_images/412310266856996864/955IBes8_normal.jpeg";
    private static final Double TEST_USER_LATITUDE = 38.8951d;
    private static final Double TEST_USER_LONGITUDE = -77.0367d;
    private static final JSONArray TEST_USER_COORDS = new JSONArray(Arrays.asList(TEST_USER_LONGITUDE, TEST_USER_LATITUDE));
    private static final String TEST_TWEET_TEXT =
            "I'm at Target (2300 W. Ben White Blvd., S. Lamar Blvd., Austin) w\\/ 3 others http://t.co/eGSHZkXH #shopping";
    private static final String TEST_TWEET_CREATED = "Thu Dec 19 22:07:04 +0000 2013";
    private static final Double TEST_TWEET_LATITUDE = 30.2500d;
    private static final Double TEST_TWEET_LONGITUDE = -97.7500d;
    private static final JSONArray TEST_TWEET_COORDS = new JSONArray(Arrays.asList(TEST_TWEET_LONGITUDE, TEST_TWEET_LATITUDE));

    private static JSONObject FULL_USER;
    private static JSONObject FULL_TWEET;

    @Mock
    private GraphVertex tweet;
    @Mock
    private GraphVertex handleConceptVertex;
    @Mock
    private GraphVertex hashtagConceptVertex;
    @Mock
    private GraphVertex urlConceptVertex;
    @Mock
    private SearchProvider searchProvider;

    JSONObject userJson = new JSONObject();
    JSONObject tupleJson = new JSONObject();

    @Override
    protected TwitterStreamingBolt createTestBolt() {
        return new TwitterStreamingBolt();
    }

    @BeforeClass
    public static void setupClass() throws Exception {
        FULL_USER = new JSONObject();
        FULL_USER.put("screen_name", TEST_USER_SCREEN_NAME);
        FULL_USER.put("name", TEST_USER_NAME);
        FULL_USER.put("statuses_count", TEST_USER_STATUS_COUNT);
        FULL_USER.put("followers_count", TEST_USER_FOLLOWERS_COUNT);
        FULL_USER.put("friends_count", TEST_USER_FRIENDS_COUNT);
        FULL_USER.put("created_at", TEST_USER_CREATED);
        FULL_USER.put("description", TEST_USER_DESCRIPTION);
        FULL_USER.put("profile_image_url", TEST_USER_PROFILE_IMAGE_URL);
        FULL_USER.put("coordinates", TEST_USER_COORDS);

        FULL_TWEET = new JSONObject();
        FULL_TWEET.put("created_at", TEST_TWEET_CREATED);
        FULL_TWEET.put("text", TEST_TWEET_TEXT);
        FULL_TWEET.put("user", FULL_USER);
        FULL_TWEET.put("coordinates", TEST_TWEET_COORDS);
    }

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        testBolt.setSearchProvider(searchProvider);
    }

    @Test
    public void testSafeExecute_NoJsonInTuple() throws Exception {
        when(tuple.getString(0)).thenReturn(null);
        testBolt.safeExecute(tuple);
        verify(artifactRepository, times(0)).save(any(Artifact.class), any(ModelUserContext.class));
        verify(graphRepository, times(0)).save(any(GraphVertex.class), any(User.class));
        verify(outputCollector, times(0)).emit(eq(tuple), any(List.class));
    }

    @Test
    public void testSafeExecute_InvalidJsonInTuple() throws Exception {
        when(tuple.getString(0)).thenReturn("asdf");
        testBolt.safeExecute(tuple);
        verify(artifactRepository, times(0)).save(any(Artifact.class), any(ModelUserContext.class));
        verify(graphRepository, times(0)).save(any(GraphVertex.class), any(User.class));
        verify(outputCollector, times(0)).emit(eq(tuple), any(List.class));
    }

    @Test
    public void testSafeExecute_NoText() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("user", FULL_USER);
        when(tuple.getString(0)).thenReturn(obj.toString());
        testBolt.safeExecute(tuple);
        verify(artifactRepository, times(0)).save(any(Artifact.class), any(ModelUserContext.class));
        verify(graphRepository, times(0)).save(any(GraphVertex.class), any(User.class));
        verify(outputCollector, times(0)).emit(eq(tuple), any(List.class));
    }

    @Test
    public void testSafeExecute_NoUser() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("text", TEST_TWEET_TEXT);
        when(tuple.getString(0)).thenReturn(obj.toString());
        testBolt.safeExecute(tuple);
        verify(artifactRepository, times(0)).save(any(Artifact.class), any(ModelUserContext.class));
        verify(graphRepository, times(0)).save(any(GraphVertex.class), any(User.class));
        verify(outputCollector, times(0)).emit(eq(tuple), any(List.class));
    }

    @Test
    public void testSafeExecute_NoScreenName() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("text", TEST_TWEET_TEXT);
        obj.put("user", new JSONObject());
        when(tuple.getString(0)).thenReturn(obj.toString());
        testBolt.safeExecute(tuple);
        verify(artifactRepository, times(0)).save(any(Artifact.class), any(ModelUserContext.class));
        verify(graphRepository, times(0)).save(any(GraphVertex.class), any(User.class));
        verify(outputCollector, times(0)).emit(eq(tuple), any(List.class));
    }

    @Test
    public void testSafeExecute() throws Exception {
        when(tuple.getString(0)).thenReturn(FULL_TWEET.toString());

        when(artifactRepository.saveToGraph(any(Artifact.class), any(ArtifactExtractedInfo.class), any(User.class))).thenReturn(tweet);
        when(graphRepository.save(tweet, systemUser)).thenReturn("");
        when(tweet.getProperty(PropertyName.TITLE)).thenReturn(TEST_TWEET_TEXT);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                tweet = (GraphVertex) invocationOnMock.getArguments()[0];
                return null;
            }
        }).when(searchProvider).add(any(GraphVertex.class), any(InputStream.class));
//
//        when(graphRepository.findVertex(handleConcept.getId(), systemUser)).thenReturn(handleConceptVertex);
//        when(graphRepository.findVertex(urlConcept.getId(), systemUser)).thenReturn(urlConceptVertex);
//        when(graphRepository.findVertex(hashtagConcept.getId(), systemUser)).thenReturn(hashtagConceptVertex);
//        when(tweet.getId()).thenReturn("");
//        when(handleConceptVertex.getId()).thenReturn("");
//        when(urlConceptVertex.getId()).thenReturn("");
//        when(hashtagConceptVertex.getId()).thenReturn("");
//        when(handleConceptVertex.getProperty(PropertyName.DISPLAY_NAME)).thenReturn("");
//        when(urlConceptVertex.getProperty(PropertyName.DISPLAY_NAME)).thenReturn("");
//        when(hashtagConceptVertex.getProperty(PropertyName.DISPLAY_NAME)).thenReturn("");
//
//        GraphVertex shopping = mock(GraphVertex.class);
//        when(graphRepository.findVertexByTitleAndType("shopping", VertexType.TYPE_ENTITY, systemUser)).thenReturn(shopping);
//        when(shopping.getId()).thenReturn("");
//
//        GraphVertex url = mock(GraphVertex.class);
//        when(graphRepository.findVertexByTitleAndType("http://t.co/egshzkxh", VertexType.TYPE_ENTITY, systemUser)).thenReturn(url);
//        when(url.getId()).thenReturn("");

        testBolt.safeExecute(tuple);

        verify(artifactRepository, times(1)).findByRowKey(anyString(), eq(modelUserContext));
        verify(artifactRepository, times(1)).save(any(Artifact.class), eq(modelUserContext));
        verify(graphRepository, times(2)).save(any(GraphVertex.class), any(User.class));
        verify(graphRepository, times(1)).saveRelationship(anyString(), anyString(), anyString(), any(User.class));
        verify(outputCollector, times(1)).emit(eq(tuple), any(List.class));
//        verify(artifactRepository, times(2)).findByRowKey(anyString(), any(ModelUserContext.class));
//        verify(artifactRepository, times(2)).save(any(Artifact.class), any(ModelUserContext.class));
//        verify(graphRepository, times(6)).save(any(GraphVertex.class), any(User.class));
//        verify(graphRepository, times(3)).saveRelationship(anyString(), anyString(), anyString(), any(User.class));
    }
}